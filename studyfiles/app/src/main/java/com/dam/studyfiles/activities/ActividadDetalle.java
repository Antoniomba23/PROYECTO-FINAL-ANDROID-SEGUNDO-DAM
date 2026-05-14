package com.dam.studyfiles.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.dam.studyfiles.R;
import com.dam.studyfiles.database.BaseDatos;
import com.dam.studyfiles.database.Favorito;
import com.dam.studyfiles.database.VotoLocal;
import com.dam.studyfiles.network.SupabaseClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActividadDetalle extends AppCompatActivity {

    private int     archivoId;
    private int     likes, dislikes, reportes;
    private String  nombre, descripcion, categoria, uploader, urlArchivo, tipoArchivo;
    private boolean esFavorito = false;

    private TextView    tvNombre, tvDesc, tvAutor, tvCategoria, tvVotos;
    private Button      btnLike, btnDislike, btnAbrir, btnFavorito, btnReportar;
    private ProgressBar pbDescargando;
    private BaseDatos   db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_detalle);

        archivoId   = getIntent().getIntExtra("archivo_id", -1);
        nombre      = getIntent().getStringExtra("nombre");
        descripcion = getIntent().getStringExtra("descripcion");
        categoria   = getIntent().getStringExtra("categoria");
        uploader    = getIntent().getStringExtra("uploader");
        urlArchivo  = getIntent().getStringExtra("url_archivo");
        tipoArchivo = getIntent().getStringExtra("tipo_archivo");
        likes       = getIntent().getIntExtra("likes", 0);
        dislikes    = getIntent().getIntExtra("dislikes", 0);
        reportes    = getIntent().getIntExtra("reportes", 0);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(nombre);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = BaseDatos.getInstance(getApplicationContext());

        tvNombre    = findViewById(R.id.tvDetalleNombre);
        tvDesc      = findViewById(R.id.tvDetalleDesc);
        tvAutor     = findViewById(R.id.tvDetalleAutor);
        tvCategoria = findViewById(R.id.tvDetalleCategoria);
        tvVotos     = findViewById(R.id.tvDetalleVotos);
        btnLike     = findViewById(R.id.btnLike);
        btnDislike  = findViewById(R.id.btnDislike);
        btnAbrir    = findViewById(R.id.btnAbrirArchivo);
        btnFavorito = findViewById(R.id.btnFavorito);
        btnReportar = findViewById(R.id.btnReportar);
        pbDescargando = findViewById(R.id.pbDescargando);

        mostrarDatos();
        comprobarEstadoLocal();

        btnLike.setOnClickListener(v -> votar("like"));
        btnDislike.setOnClickListener(v -> votar("dislike"));
        btnAbrir.setOnClickListener(v -> abrirArchivo(false));
        btnFavorito.setOnClickListener(v -> toggleFavorito());
        btnReportar.setOnClickListener(v -> confirmarReporte());
    }

    private void mostrarDatos() {
        tvNombre.setText(nombre);
        tvDesc.setText(descripcion != null && !descripcion.isEmpty() ? descripcion : "Sin descripción");
        tvAutor.setText("Subido por: " + (uploader != null ? uploader : "Anónimo"));
        tvCategoria.setText("Categoría: " + (categoria != null ? categoria : "—"));
        actualizarVotos();
    }

    private void actualizarVotos() {
        tvVotos.setText("👍 " + likes + "   👎 " + dislikes);
    }

    // ── Estado local (favorito + voto) ────────────────────────────────────────

    private void comprobarEstadoLocal() {
        Executors.newSingleThreadExecutor().execute(() -> {
            String  voto = db.votoLocalDao().obtenerVoto(archivoId);
            boolean fav  = db.favoritoDao().esFavorito(archivoId) > 0;
            runOnUiThread(() -> {
                esFavorito = fav;
                actualizarBotonFavorito();
                if (voto != null) {
                    btnLike.setEnabled(false);
                    btnDislike.setEnabled(false);
                    btnLike.setText("like".equals(voto) ? "✅ Me gusta" : "👍 Me gusta");
                    btnDislike.setText("dislike".equals(voto) ? "✅ No me gusta" : "👎 No me gusta");
                }
            });
        });
    }

    // ── Votar ─────────────────────────────────────────────────────────────────

    private void votar(String tipo) {
        btnLike.setEnabled(false);
        btnDislike.setEnabled(false);

        if ("like".equals(tipo)) likes++;
        else dislikes++;
        actualizarVotos();

        Executors.newSingleThreadExecutor().execute(() ->
                db.votoLocalDao().insertar(new VotoLocal(archivoId, tipo)));

        Map<String, Object> campos = new HashMap<>();
        campos.put("likes",    likes);
        campos.put("dislikes", dislikes);

        SupabaseClient.getApi().actualizarArchivo("eq." + archivoId, campos)
                .enqueue(new Callback<Void>() {
                    @Override public void onResponse(Call<Void> c, Response<Void> r) {
                        if (dislikes >= 10) eliminarPorMalosDislikes();
                    }
                    @Override public void onFailure(Call<Void> c, Throwable t) {
                        Toast.makeText(ActividadDetalle.this,
                                "Error al guardar voto", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void eliminarPorMalosDislikes() {
        SupabaseClient.getApi().eliminarArchivo("eq." + archivoId)
                .enqueue(new Callback<Void>() {
                    @Override public void onResponse(Call<Void> c, Response<Void> r) {
                        Toast.makeText(ActividadDetalle.this,
                                "Archivo eliminado por exceso de 'No me gusta'",
                                Toast.LENGTH_LONG).show();
                        finish();
                    }
                    @Override public void onFailure(Call<Void> c, Throwable t) {}
                });
    }

    // ── Abrir archivo ─────────────────────────────────────────────────────────

    /**
     * @param soloLocal true → abre solo el archivo local (modo sin internet),
     *                  false → intenta local primero, si no hay, abre URL
     */
    private void abrirArchivo(boolean soloLocal) {
        Executors.newSingleThreadExecutor().execute(() -> {
            File local = obtenerArchivoLocal();
            runOnUiThread(() -> {
                if (local != null && local.exists() && local.length() > 0) {
                    abrirArchivoLocal(local);
                } else if (soloLocal) {
                    Toast.makeText(this,
                            "Archivo no descargado aún. Conecta a internet y ábrelo desde el detalle.",
                            Toast.LENGTH_LONG).show();
                } else {
                    if (urlArchivo != null && !urlArchivo.isEmpty()) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(urlArchivo)));
                    } else {
                        Toast.makeText(this, "URL no disponible", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }

    private void abrirArchivoLocal(File file) {
        try {
            Uri uri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", file);
            String mime = getMimeType(tipoArchivo);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, mime);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Abrir con..."));
        } catch (Exception e) {
            Toast.makeText(this, "No se puede abrir: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    // ── Favoritos + descarga ───────────────────────────────────────────────────

    private void toggleFavorito() {
        if (esFavorito) {
            // Quitar de favoritos y borrar archivo local
            Executors.newSingleThreadExecutor().execute(() -> {
                File local = obtenerArchivoLocal();
                if (local != null && local.exists()) local.delete();
                db.favoritoDao().eliminarPorId(archivoId);
                esFavorito = false;
                runOnUiThread(() -> {
                    actualizarBotonFavorito();
                    Toast.makeText(this, "Eliminado de favoritos", Toast.LENGTH_SHORT).show();
                });
            });
        } else {
            // Añadir a favoritos y descargar el archivo
            descargarYGuardarFavorito();
        }
    }

    private void descargarYGuardarFavorito() {
        if (urlArchivo == null || urlArchivo.isEmpty()) {
            guardarFavoritoSinArchivo();
            return;
        }

        pbDescargando.setVisibility(View.VISIBLE);
        btnFavorito.setEnabled(false);
        btnFavorito.setText("⬇ Descargando...");

        new Thread(() -> {
            String rutaLocal = null;
            try {
                File dir = new File(getFilesDir(), "favoritos");
                if (!dir.exists()) dir.mkdirs();

                String ext  = tipoArchivo != null ? "." + tipoArchivo : "";
                File   dest = new File(dir, "fav_" + archivoId + ext);

                URL url = new URL(urlArchivo);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(30000);
                conn.connect();

                InputStream  is  = conn.getInputStream();
                FileOutputStream fos = new FileOutputStream(dest);
                byte[] buf = new byte[8192];
                int n;
                while ((n = is.read(buf)) != -1) fos.write(buf, 0, n);
                fos.close();
                is.close();
                conn.disconnect();

                rutaLocal = dest.getAbsolutePath();
            } catch (Exception e) {
                // Si falla la descarga, guardamos el favorito sin archivo local
            }

            final String finalRuta = rutaLocal;
            runOnUiThread(() -> {
                pbDescargando.setVisibility(View.GONE);
                btnFavorito.setEnabled(true);
                Executors.newSingleThreadExecutor().execute(() -> {
                    Favorito fav = new Favorito(archivoId, nombre, descripcion, categoria,
                            uploader, urlArchivo, tipoArchivo, likes, dislikes);
                    fav.rutaLocal = finalRuta;
                    db.favoritoDao().insertar(fav);
                    esFavorito = true;
                    runOnUiThread(() -> {
                        actualizarBotonFavorito();
                        String msg = finalRuta != null
                                ? "✅ Favorito guardado y descargado (disponible sin internet)"
                                : "☆ Favorito guardado (sin descarga local)";
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    });
                });
            });
        }).start();
    }

    private void guardarFavoritoSinArchivo() {
        Executors.newSingleThreadExecutor().execute(() -> {
            db.favoritoDao().insertar(new Favorito(archivoId, nombre, descripcion, categoria,
                    uploader, urlArchivo, tipoArchivo, likes, dislikes));
            esFavorito = true;
            runOnUiThread(() -> {
                actualizarBotonFavorito();
                Toast.makeText(this, "Añadido a favoritos", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void actualizarBotonFavorito() {
        btnFavorito.setText(esFavorito ? "★ Quitar favorito" : "☆ Añadir a favoritos");
    }

    // ── Utilidades ────────────────────────────────────────────────────────────

    /** Devuelve el archivo local si existe */
    private File obtenerArchivoLocal() {
        if (tipoArchivo == null) return null;
        String ext  = "." + tipoArchivo;
        File   file = new File(new File(getFilesDir(), "favoritos"), "fav_" + archivoId + ext);
        return file;
    }

    private String getMimeType(String ext) {
        if (ext == null) return "*/*";
        switch (ext.toLowerCase()) {
            case "pdf":  return "application/pdf";
            case "doc":  return "application/msword";
            case "docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "ppt":  return "application/vnd.ms-powerpoint";
            case "pptx": return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "xls":  return "application/vnd.ms-excel";
            case "xlsx": return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "png":  return "image/png";
            case "jpg":  case "jpeg": return "image/jpeg";
            case "txt":  return "text/plain";
            case "zip":  return "application/zip";
            default:     return "*/*";
        }
    }

    // ── Reporte ───────────────────────────────────────────────────────────────

    private void confirmarReporte() {
        new AlertDialog.Builder(this)
                .setTitle("Reportar archivo")
                .setMessage("¿Quieres reportar este archivo como inapropiado?")
                .setPositiveButton("Reportar", (d, w) -> enviarReporte())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void enviarReporte() {
        reportes++;
        Map<String, Object> campos = new HashMap<>();
        campos.put("reportes", reportes);
        SupabaseClient.getApi().actualizarArchivo("eq." + archivoId, campos)
                .enqueue(new Callback<Void>() {
                    @Override public void onResponse(Call<Void> c, Response<Void> r) {
                        Toast.makeText(ActividadDetalle.this,
                                "Archivo reportado. Gracias.", Toast.LENGTH_SHORT).show();
                        btnReportar.setEnabled(false);
                    }
                    @Override public void onFailure(Call<Void> c, Throwable t) {
                        Toast.makeText(ActividadDetalle.this,
                                "Error al reportar", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
