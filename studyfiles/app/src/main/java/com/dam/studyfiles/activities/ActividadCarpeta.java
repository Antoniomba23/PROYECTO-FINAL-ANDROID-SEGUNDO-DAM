package com.dam.studyfiles.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dam.studyfiles.R;
import com.dam.studyfiles.adapters.AdaptadorMisArchivos;
import com.dam.studyfiles.models.MiArchivo;
import com.dam.studyfiles.network.SupabaseClient;
import com.dam.studyfiles.utils.DispositivoUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;

public class ActividadCarpeta extends AppCompatActivity {

    private RecyclerView         rv;
    private ProgressBar          pb;
    private TextView             tvVacio;
    private AdaptadorMisArchivos adaptador;
    private List<MiArchivo>      lista = new ArrayList<>();

    private int    carpetaId;
    private String carpetaNombre;
    private String uuid;

    private Uri    uriSubir;
    private String nombreArchivoLocal;

    private final ActivityResultLauncher<String[]> selectorArchivo =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    uriSubir          = uri;
                    nombreArchivoLocal = obtenerNombreArchivo(uri);
                    mostrarDialogoSubir();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_carpeta);

        carpetaId     = getIntent().getIntExtra("carpeta_id", -1);
        carpetaNombre = getIntent().getStringExtra("carpeta_nombre");
        uuid          = DispositivoUtils.getUUID(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(carpetaNombre != null ? carpetaNombre : "Carpeta");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        rv      = findViewById(R.id.rvArchivosCarpeta);
        pb      = findViewById(R.id.pbCargandoCarpeta);
        tvVacio = findViewById(R.id.tvVacioCarpeta);

        adaptador = new AdaptadorMisArchivos(lista, this::onArchivoClick, this::onArchivoLongClick);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adaptador);

        FloatingActionButton fab = findViewById(R.id.fabSubirACarpeta);
        fab.setOnClickListener(v -> selectorArchivo.launch(new String[]{"*/*"}));

        cargarArchivos();
    }

    private void cargarArchivos() {
        pb.setVisibility(View.VISIBLE);
        SupabaseClient.getMiNube()
                .getMisArchivosDeCarpeta("eq." + uuid, "eq." + carpetaId)
                .enqueue(new Callback<List<MiArchivo>>() {
                    @Override
                    public void onResponse(Call<List<MiArchivo>> c, retrofit2.Response<List<MiArchivo>> r) {
                        pb.setVisibility(View.GONE);
                        if (r.isSuccessful() && r.body() != null) {
                            lista.clear();
                            lista.addAll(r.body());
                            adaptador.actualizar(lista);
                            tvVacio.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
                        }
                    }
                    @Override
                    public void onFailure(Call<List<MiArchivo>> c, Throwable t) {
                        pb.setVisibility(View.GONE);
                        Toast.makeText(ActividadCarpeta.this, "Sin conexión", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void onArchivoClick(MiArchivo a) {
        if (a.urlArchivo != null && !a.urlArchivo.isEmpty()) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(a.urlArchivo)));
        }
    }

    private void onArchivoLongClick(MiArchivo a, View anchor) {
        new AlertDialog.Builder(this)
                .setTitle(a.nombre)
                .setItems(new String[]{"🗑 Eliminar"}, (d, w) -> eliminarArchivo(a))
                .show();
    }

    private void eliminarArchivo(MiArchivo a) {
        SupabaseClient.getMiNube().eliminarMiArchivo("eq." + a.id)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> c, retrofit2.Response<Void> r) {
                        Toast.makeText(ActividadCarpeta.this,
                                "Archivo eliminado", Toast.LENGTH_SHORT).show();
                        cargarArchivos();
                    }
                    @Override
                    public void onFailure(Call<Void> c, Throwable t) {}
                });
    }

    private void mostrarDialogoSubir() {
        EditText etNombre = new EditText(this);
        etNombre.setHint("Nombre del archivo");
        etNombre.setText(nombreArchivoLocal);

        new AlertDialog.Builder(this)
                .setTitle("Subir a esta carpeta")
                .setView(etNombre)
                .setPositiveButton("Subir", (d, w) -> {
                    String nombre = etNombre.getText().toString().trim();
                    if (nombre.isEmpty()) nombre = nombreArchivoLocal;
                    subirArchivo(nombre);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void subirArchivo(String nombre) {
        pb.setVisibility(View.VISIBLE);
        String finalNombre = nombre;

        new Thread(() -> {
            try {
                InputStream is = getContentResolver().openInputStream(uriSubir);
                if (is == null) throw new Exception("No se pudo leer");

                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                byte[] chunk = new byte[8192];
                int n;
                while ((n = is.read(chunk)) != -1) buf.write(chunk, 0, n);
                is.close();
                byte[] bytes = buf.toByteArray();

                String ext       = obtenerExtension(nombreArchivoLocal);
                String mime      = getContentResolver().getType(uriSubir);
                if (mime == null) mime = "application/octet-stream";
                String ruta      = "minube/" + uuid + "/" + carpetaId + "/"
                        + System.currentTimeMillis() + "_" + nombreArchivoLocal;

                okhttp3.RequestBody body = okhttp3.RequestBody.create(bytes,
                        MediaType.parse(mime));
                Request req = new Request.Builder()
                        .url(SupabaseClient.URL_BASE + "storage/v1/object/mi-nube/" + ruta)
                        .addHeader("apikey", SupabaseClient.API_KEY)
                        .addHeader("Authorization", "Bearer " + SupabaseClient.API_KEY)
                        .addHeader("Content-Type", mime)
                        .post(body).build();

                Response res = new OkHttpClient().newCall(req).execute();
                if (res.isSuccessful()) {
                    String url = SupabaseClient.URL_BASE
                            + "storage/v1/object/public/mi-nube/" + ruta;
                    Map<String, Object> data = new HashMap<>();
                    data.put("nombre",       finalNombre);
                    data.put("carpeta_id",   carpetaId);
                    data.put("usuario_id",   uuid);
                    data.put("url_archivo",  url);
                    data.put("tipo_archivo", ext);
                    data.put("fecha_subida", System.currentTimeMillis());

                    SupabaseClient.getMiNube().subirMiArchivo(data)
                            .enqueue(new Callback<List<MiArchivo>>() {
                                @Override
                                public void onResponse(Call<List<MiArchivo>> c,
                                                       retrofit2.Response<List<MiArchivo>> r) {
                                    runOnUiThread(() -> {
                                        pb.setVisibility(View.GONE);
                                        Toast.makeText(ActividadCarpeta.this,
                                                "Archivo subido!", Toast.LENGTH_SHORT).show();
                                        cargarArchivos();
                                    });
                                }
                                @Override public void onFailure(Call<List<MiArchivo>> c, Throwable t) {
                                    runOnUiThread(() -> pb.setVisibility(View.GONE));
                                }
                            });
                } else {
                    runOnUiThread(() -> {
                        pb.setVisibility(View.GONE);
                        Toast.makeText(this, "Error al subir archivo", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private String obtenerNombreArchivo(Uri uri) {
        String p = uri.getLastPathSegment();
        if (p != null && p.contains("/")) p = p.substring(p.lastIndexOf("/") + 1);
        return p != null ? p : "archivo";
    }

    private String obtenerExtension(String nombre) {
        if (nombre != null && nombre.contains("."))
            return nombre.substring(nombre.lastIndexOf(".") + 1).toLowerCase();
        return "bin";
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}
