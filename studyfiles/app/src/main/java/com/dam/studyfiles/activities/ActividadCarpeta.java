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
import com.dam.studyfiles.models.MiCarpeta;
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

    private RecyclerView         rv, rvCarpetas;
    private ProgressBar          pb;
    private TextView             tvVacio;
    
    private AdaptadorMisArchivos adaptador;
    private List<MiArchivo>      lista = new ArrayList<>();
    
    private com.dam.studyfiles.adapters.AdaptadorCarpetas adaptadorCarpetas;
    private List<MiCarpeta>      listaCarpetas = new ArrayList<>();

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

        rv          = findViewById(R.id.rvArchivosCarpeta);
        rvCarpetas  = findViewById(R.id.rvCarpetasHijas);
        pb          = findViewById(R.id.pbCargandoCarpeta);
        tvVacio     = findViewById(R.id.tvVacioCarpeta);

        adaptador = new AdaptadorMisArchivos(lista,
                this::onArchivoClick,
                this::onArchivoLongClick);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adaptador);

        adaptadorCarpetas = new com.dam.studyfiles.adapters.AdaptadorCarpetas(listaCarpetas,
                carpeta -> {
                    Intent i = new Intent(this, ActividadCarpeta.class);
                    i.putExtra("carpeta_id",     carpeta.id);
                    i.putExtra("carpeta_nombre", carpeta.nombre);
                    startActivity(i);
                },
                (carpeta, anchor) -> mostrarMenuCarpeta(carpeta)
        );
        rvCarpetas.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 2));
        rvCarpetas.setAdapter(adaptadorCarpetas);

        FloatingActionButton fab = findViewById(R.id.fabSubirACarpeta);
        fab.setOnClickListener(v -> mostrarMenuFab());

        cargarArchivosYCarpetas();
    }

    private void mostrarMenuFab() {
        new AlertDialog.Builder(this)
                .setTitle("Carpeta")
                .setItems(new String[]{"📁 Crear Subcarpeta", "☁️ Subir Archivo"}, (d, w) -> {
                    if (w == 0) crearSubcarpeta();
                    else selectorArchivo.launch(new String[]{"*/*"});
                })
                .show();
    }

    private void crearSubcarpeta() {
        String[] colores = {"#1565C0","#2E7D32","#E65100","#6A1B9A","#C62828","#00695C"};
        EditText et = new EditText(this);
        et.setHint("Nombre de la subcarpeta");
        new AlertDialog.Builder(this)
                .setTitle("Nueva Subcarpeta")
                .setView(et)
                .setPositiveButton("Crear", (d, w) -> {
                    String nombre = et.getText().toString().trim();
                    if (nombre.isEmpty()) return;
                    int idx = (int)(Math.random() * colores.length);
                    Map<String, Object> body = new HashMap<>();
                    body.put("nombre",         nombre);
                    body.put("usuario_id",     uuid);
                    body.put("carpeta_padre_id", carpetaId);
                    body.put("color",          colores[idx]);
                    body.put("fecha_creacion", System.currentTimeMillis());
                    SupabaseClient.getMiNube().crearCarpeta(body)
                            .enqueue(new Callback<List<MiCarpeta>>() {
                                @Override public void onResponse(Call<List<MiCarpeta>> c, retrofit2.Response<List<MiCarpeta>> r) { cargarArchivosYCarpetas(); }
                                @Override public void onFailure(Call<List<MiCarpeta>> c, Throwable t) {}
                            });
                })
                .setNegativeButton("Cancelar", null).show();
    }

    private void mostrarMenuCarpeta(MiCarpeta carpeta) {
        new AlertDialog.Builder(this)
                .setTitle(carpeta.nombre)
                .setItems(new String[]{"✏️ Renombrar", "🗑 Eliminar"}, (d, w) -> {
                    if (w == 0) renombrarCarpeta(carpeta);
                    else eliminarCarpeta(carpeta);
                }).show();
    }

    private void renombrarCarpeta(MiCarpeta carpeta) {
        EditText et = new EditText(this);
        et.setText(carpeta.nombre);
        new AlertDialog.Builder(this)
                .setTitle("Renombrar subcarpeta")
                .setView(et)
                .setPositiveButton("Guardar", (d, w) -> {
                    String nuevo = et.getText().toString().trim();
                    if (nuevo.isEmpty()) return;
                    Map<String, Object> body = new HashMap<>();
                    body.put("nombre", nuevo);
                    SupabaseClient.getMiNube().renombrarCarpeta("eq." + carpeta.id, body)
                            .enqueue(new Callback<Void>() {
                                @Override public void onResponse(Call<Void> c, retrofit2.Response<Void> r) { cargarArchivosYCarpetas(); }
                                @Override public void onFailure(Call<Void> c, Throwable t) {}
                            });
                })
                .setNegativeButton("Cancelar", null).show();
    }

    private void eliminarCarpeta(MiCarpeta carpeta) {
        new AlertDialog.Builder(this)
                .setMessage("¿Eliminar la subcarpeta '" + carpeta.nombre + "' y todo su contenido?")
                .setPositiveButton("Eliminar", (d, w) ->
                        SupabaseClient.getMiNube().eliminarCarpeta("eq." + carpeta.id)
                                .enqueue(new Callback<Void>() {
                                    @Override public void onResponse(Call<Void> c, retrofit2.Response<Void> r) { cargarArchivosYCarpetas(); }
                                    @Override public void onFailure(Call<Void> c, Throwable t) {}
                                }))
                .setNegativeButton("Cancelar", null).show();
    }

    private void cargarArchivosYCarpetas() {
        pb.setVisibility(View.VISIBLE);

        // Cargar Subcarpetas
        SupabaseClient.getMiNube().getMisCarpetasHijas("eq." + uuid, "eq." + carpetaId)
                .enqueue(new Callback<List<MiCarpeta>>() {
                    @Override
                    public void onResponse(Call<List<MiCarpeta>> c, retrofit2.Response<List<MiCarpeta>> r) {
                        if (r.isSuccessful() && r.body() != null) {
                            listaCarpetas.clear();
                            listaCarpetas.addAll(r.body());
                            adaptadorCarpetas.actualizar(listaCarpetas);
                            verificarVacio();
                        }
                    }
                    @Override public void onFailure(Call<List<MiCarpeta>> c, Throwable t) {}
                });

        // Cargar Archivos
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
                            verificarVacio();
                        }
                    }
                    @Override
                    public void onFailure(Call<List<MiArchivo>> c, Throwable t) {
                        pb.setVisibility(View.GONE);
                        Toast.makeText(ActividadCarpeta.this, "Sin conexión", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void verificarVacio() {
        tvVacio.setVisibility(lista.isEmpty() && listaCarpetas.isEmpty() ? View.VISIBLE : View.GONE);
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
                        cargarArchivosYCarpetas();
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

                String mime      = getContentResolver().getType(uriSubir);
                if (mime == null) mime = "application/octet-stream";
                // Usar MIME como fallback si el nombre no tiene extensión
                String ext = obtenerExtension(nombreArchivoLocal);
                if (ext.equals("bin")) ext = extensionDesdeMime(mime);
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
                                        cargarArchivosYCarpetas();
                                    });
                                }
                                @Override public void onFailure(Call<List<MiArchivo>> c, Throwable t) {
                                    runOnUiThread(() -> pb.setVisibility(View.GONE));
                                }
                            });
                } else {
                    String errorBody = "";
                    try { if (res.body() != null) errorBody = res.body().string(); } catch (Exception ignored) {}
                    final String msg = "Error " + res.code() + ": " + errorBody;
                    runOnUiThread(() -> {
                        pb.setVisibility(View.GONE);
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                        android.util.Log.e("SUBIDA_NUBE", msg);
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

    /** Obtiene el nombre real del archivo (con extensión) usando ContentResolver */
    private String obtenerNombreArchivo(Uri uri) {
        // 1. Intentar obtener DISPLAY_NAME del ContentResolver (el más fiable)
        try (android.database.Cursor cursor = getContentResolver().query(
                uri, new String[]{android.provider.OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                String name = cursor.getString(0);
                if (name != null && !name.isEmpty()) return name;
            }
        } catch (Exception ignored) {}
        // 2. Fallback: último segmento del path
        String p = uri.getLastPathSegment();
        if (p != null && p.contains("/")) p = p.substring(p.lastIndexOf("/") + 1);
        return (p != null && !p.isEmpty()) ? p : "archivo";
    }

    /** Extrae la extensión del nombre; si no tiene, deduce desde el MIME type */
    private String obtenerExtension(String nombre) {
        if (nombre != null && nombre.contains("."))
            return nombre.substring(nombre.lastIndexOf(".") + 1).toLowerCase();
        return "bin";
    }

    /** Deduce la extensión a partir del MIME type cuando el nombre no la tiene */
    private String extensionDesdeMime(String mime) {
        if (mime == null) return "bin";
        switch (mime) {
            case "application/pdf":  return "pdf";
            case "image/jpeg":       return "jpg";
            case "image/png":        return "png";
            case "image/gif":        return "gif";
            case "image/webp":       return "webp";
            case "application/msword": return "doc";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document": return "docx";
            case "application/vnd.ms-powerpoint": return "ppt";
            case "application/vnd.openxmlformats-officedocument.presentationml.presentation": return "pptx";
            case "application/vnd.ms-excel": return "xls";
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet": return "xlsx";
            case "text/plain":       return "txt";
            case "application/zip": return "zip";
            default: return "bin";
        }
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}
