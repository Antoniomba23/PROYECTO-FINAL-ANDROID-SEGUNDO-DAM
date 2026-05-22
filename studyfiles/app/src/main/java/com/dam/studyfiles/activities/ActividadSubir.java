package com.dam.studyfiles.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.dam.studyfiles.R;
import com.dam.studyfiles.models.SubirArchivoRequest;
import com.dam.studyfiles.network.SupabaseClient;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;

public class ActividadSubir extends AppCompatActivity {

    private static final String PREFS    = "studyfiles_prefs";
    private static final String KEY_AUTOR = "nombre_autor";

    private TextInputEditText etNombre, etDescripcion, etAutor, etInstitucion, etNivelEstudios;
    private Spinner           spCategoria;
    private Button            btnSeleccionar, btnSubir;
    private TextView          tvArchivoSeleccionado;
    private ProgressBar       pb;

    private Uri    uriSeleccionado;
    private String nombreArchivoLocal;

    private final String[] CATEGORIAS = {
            "Historia", "Matemáticas", "Lengua", "Ciencias",
            "Informática", "Inglés", "Arte y Música", 
            "Filosofía", "Economía", "Física y Química", "Biología", "Otros"
    };

    private final ActivityResultLauncher<String[]> selectorArchivo =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    uriSeleccionado     = uri;
                    nombreArchivoLocal  = obtenerNombreArchivo(uri);
                    tvArchivoSeleccionado.setText(nombreArchivoLocal);
                    tvArchivoSeleccionado.setVisibility(View.VISIBLE);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_subir);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Subir archivo");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etNombre              = findViewById(R.id.etNombreArchivo);
        etDescripcion         = findViewById(R.id.etDescripcion);
        etAutor               = findViewById(R.id.etAutor);
        etInstitucion         = findViewById(R.id.etInstitucion);
        etNivelEstudios       = findViewById(R.id.etNivelEstudios);
        spCategoria           = findViewById(R.id.spCategoria);
        btnSeleccionar        = findViewById(R.id.btnSeleccionarArchivo);
        btnSubir              = findViewById(R.id.btnSubir);
        tvArchivoSeleccionado = findViewById(R.id.tvArchivoSeleccionado);
        pb                    = findViewById(R.id.pbSubiendo);

        // Cargar nombre autor o usuario logueado
        if (com.dam.studyfiles.utils.DispositivoUtils.isLogged(this)) {
            etAutor.setText(com.dam.studyfiles.utils.DispositivoUtils.getUsuarioNombre(this));
            etAutor.setEnabled(false);
        } else {
            SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
            String autorGuardado = prefs.getString(KEY_AUTOR, "");
            if (!autorGuardado.isEmpty()) etAutor.setText(autorGuardado);
        }

        // Spinner de categorías
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, CATEGORIAS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategoria.setAdapter(adapter);

        btnSeleccionar.setOnClickListener(v ->
                selectorArchivo.launch(new String[]{"*/*"}));

        btnSubir.setOnClickListener(v -> validarYSubir());
    }

    private void validarYSubir() {
        String nombre      = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
        String descripcion = etDescripcion.getText() != null ? etDescripcion.getText().toString().trim() : "";
        String autor       = etAutor.getText() != null ? etAutor.getText().toString().trim() : "";
        String categoria   = CATEGORIAS[spCategoria.getSelectedItemPosition()];
        String institucion = etInstitucion.getText() != null ? etInstitucion.getText().toString().trim() : "";
        String nivelEstudios = etNivelEstudios.getText() != null ? etNivelEstudios.getText().toString().trim() : "";

        if (nombre.isEmpty()) {
            etNombre.setError("El nombre es obligatorio");
            return;
        }
        if (autor.isEmpty()) {
            etAutor.setError("Escribe tu nombre para que otros sepan quién lo sube");
            return;
        }
        if (uriSeleccionado == null) {
            Toast.makeText(this, "Selecciona un archivo primero", Toast.LENGTH_SHORT).show();
            return;
        }

        // Guardar autor para la próxima vez (si no está logueado)
        if (!com.dam.studyfiles.utils.DispositivoUtils.isLogged(this)) {
            getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                    .putString(KEY_AUTOR, autor).apply();
        }

        pb.setVisibility(View.VISIBLE);
        btnSubir.setEnabled(false);

        subirAStorage(nombre, descripcion, autor, categoria, institucion, nivelEstudios);
    }

    private void subirAStorage(String nombre, String descripcion,
                                String autor, String categoria, String institucion, String nivelEstudios) {
        new Thread(() -> {
            try {
                InputStream is = getContentResolver().openInputStream(uriSeleccionado);
                if (is == null) throw new Exception("No se pudo leer el archivo");

                // Leer bytes compatible con Java 8
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] chunk = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(chunk)) != -1) {
                    buffer.write(chunk, 0, bytesRead);
                }
                is.close();
                byte[] bytes = buffer.toByteArray();

                String mimeType = getContentResolver().getType(uriSeleccionado);
                if (mimeType == null) mimeType = "application/octet-stream";
                
                String extension = obtenerExtension(nombreArchivoLocal);
                if (extension.equals("bin")) extension = extensionDesdeMime(mimeType);
                
                String rutaStorage = "archivos/" + System.currentTimeMillis() + "_" + nombreArchivoLocal;
                // Si el nombre local no tiene extensión, se la añadimos para que el storage lo reconozca
                if (!nombreArchivoLocal.contains(".") && !extension.equals("bin")) {
                    rutaStorage += "." + extension;
                }

                RequestBody body = RequestBody.create(bytes, MediaType.parse(mimeType));
                Request request = new Request.Builder()
                        .url(SupabaseClient.URL_BASE + "storage/v1/object/archivos-estudio/" + rutaStorage)
                        .addHeader("apikey", SupabaseClient.API_KEY)
                        .addHeader("Authorization", "Bearer " + SupabaseClient.API_KEY)
                        .addHeader("Content-Type", mimeType)
                        .post(body)
                        .build();

                OkHttpClient client = new OkHttpClient();
                Response storageResponse = client.newCall(request).execute();

                if (storageResponse.isSuccessful()) {
                    String urlPublica = SupabaseClient.URL_BASE
                            + "storage/v1/object/public/archivos-estudio/" + rutaStorage;
                    guardarMetadatos(nombre, descripcion, autor, categoria, urlPublica, extension, institucion, nivelEstudios);
                } else {
                    String errorBody = storageResponse.body() != null
                            ? storageResponse.body().string() : "Error desconocido";
                    runOnUiThread(() -> {
                        pb.setVisibility(View.GONE);
                        btnSubir.setEnabled(true);
                        Toast.makeText(this, "Error al subir: " + errorBody, Toast.LENGTH_LONG).show();
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    pb.setVisibility(View.GONE);
                    btnSubir.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void guardarMetadatos(String nombre, String descripcion, String autor,
                                   String categoria, String url, String tipo, String institucion, String nivelEstudios) {
        SubirArchivoRequest req = new SubirArchivoRequest();
        req.nombre      = nombre;
        req.descripcion = descripcion;
        req.uploader    = autor;
        req.categoria   = categoria;
        req.urlArchivo  = url;
        req.tipoArchivo = tipo;
        req.institucion = institucion.isEmpty() ? null : institucion;
        req.nivelEstudios = nivelEstudios.isEmpty() ? null : nivelEstudios;
        
        String userId = com.dam.studyfiles.utils.DispositivoUtils.getUsuarioId(this);
        req.usuarioId = userId != null ? "user_" + userId : com.dam.studyfiles.utils.DispositivoUtils.getUUID(this);

        SupabaseClient.getApi().subirArchivo(req).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> c, retrofit2.Response<Void> r) {
                pb.setVisibility(View.GONE);
                if (r.isSuccessful()) {
                    Toast.makeText(ActividadSubir.this,
                            "Archivo subido correctamente!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    btnSubir.setEnabled(true);
                    Toast.makeText(ActividadSubir.this,
                            "Error al guardar: " + r.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> c, Throwable t) {
                pb.setVisibility(View.GONE);
                btnSubir.setEnabled(true);
                Toast.makeText(ActividadSubir.this,
                        "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String obtenerNombreArchivo(Uri uri) {
        try (android.database.Cursor cursor = getContentResolver().query(
                uri, new String[]{android.provider.OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                String name = cursor.getString(0);
                if (name != null && !name.isEmpty()) return name;
            }
        } catch (Exception ignored) {}
        String p = uri.getLastPathSegment();
        if (p != null && p.contains("/")) p = p.substring(p.lastIndexOf("/") + 1);
        return (p != null && !p.isEmpty()) ? p : "archivo_" + System.currentTimeMillis();
    }

    private String obtenerExtension(String nombre) {
        if (nombre != null && nombre.contains("."))
            return nombre.substring(nombre.lastIndexOf(".") + 1).toLowerCase();
        return "bin";
    }

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
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
