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

import org.json.JSONObject;
import org.json.JSONException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActividadDetalle extends AppCompatActivity {

    private int     archivoId;
    private int     likes, dislikes, reportes;
    private String  nombre, descripcion, categoria, uploader, urlArchivo, tipoArchivo, institucion, nivelEstudios;
    private boolean esFavorito = false;

    private TextView    tvNombre, tvDesc, tvAutor, tvCategoria, tvVotos;
    private Button      btnLike, btnDislike, btnAbrir, btnFavorito, btnReportar;
    private ProgressBar pbDescargando;
    private BaseDatos   db;

    // Comentarios
    private androidx.recyclerview.widget.RecyclerView rvComentarios;
    private android.widget.LinearLayout llComentarInput;
    private TextView tvAvisoLoginComentario;
    private android.widget.EditText etNuevoComentario;
    private com.google.android.material.floatingactionbutton.FloatingActionButton btnEnviarComentario;
    private com.dam.studyfiles.adapters.AdaptadorComentarios adaptadorComentarios;
    private java.util.List<com.dam.studyfiles.models.Comentario> listaComentarios = new java.util.ArrayList<>();

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
        institucion = getIntent().getStringExtra("institucion");
        nivelEstudios = getIntent().getStringExtra("nivel_estudios");

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

        // Inicializar vistas de comentarios
        rvComentarios = findViewById(R.id.rvComentarios);
        llComentarInput = findViewById(R.id.llComentarInput);
        tvAvisoLoginComentario = findViewById(R.id.tvAvisoLoginComentario);
        etNuevoComentario = findViewById(R.id.etNuevoComentario);
        btnEnviarComentario = findViewById(R.id.btnEnviarComentario);

        mostrarDatos();
        comprobarEstadoLocal();
        configurarComentarios();

        btnLike.setOnClickListener(v -> votar("like"));
        btnDislike.setOnClickListener(v -> votar("dislike"));
        btnAbrir.setOnClickListener(v -> abrirArchivo(false));
        btnFavorito.setOnClickListener(v -> toggleFavorito());
        btnReportar.setOnClickListener(v -> confirmarReporte());
    }

    private void mostrarDatos() {
        tvNombre.setText(nombre);
        tvDesc.setText(descripcion != null && !descripcion.isEmpty() ? descripcion : "Sin descripción");
        
        StringBuilder autorInfo = new StringBuilder("Subido por: " + (uploader != null ? uploader : "Anónimo"));
        if (institucion != null && !institucion.isEmpty()) {
            autorInfo.append(" (").append(institucion).append(")");
        }
        if (nivelEstudios != null && !nivelEstudios.isEmpty()) {
            autorInfo.append(" - ").append(nivelEstudios);
        }
        tvAutor.setText(autorInfo.toString());
        
        tvCategoria.setText("Categoría: " + (categoria != null ? categoria : "—"));
        actualizarVotos();
    }

    private void actualizarVotos() {
        tvVotos.setText("👍 " + likes + "   👎 " + dislikes);
    }

    // ── COMENTARIOS ───────────────────────────────────────────────────────────

    private void configurarComentarios() {
        String currentUserId = com.dam.studyfiles.utils.DispositivoUtils.getUsuarioId(this);
        String uuid = com.dam.studyfiles.utils.DispositivoUtils.getUUID(this);
        String myId = currentUserId != null ? "user_" + currentUserId : uuid;

        // Comprobar si soy el dueño del archivo para mostrar botón de eliminar
        Button btnEliminarPublicacion = new Button(this);
        btnEliminarPublicacion.setText("Eliminar Publicación");
        btnEliminarPublicacion.setBackgroundColor(android.graphics.Color.RED);
        btnEliminarPublicacion.setTextColor(android.graphics.Color.WHITE);
        
        // Asumiendo que tenemos que pedir al backend el archivo para saber su usuario_id,
        // o pasarlo por el Intent desde el adaptador. Vamos a comprobar el Intent.
        String fileUserId = getIntent().getStringExtra("usuario_id");
        if (fileUserId != null && fileUserId.equals(myId)) {
            ((android.widget.LinearLayout) btnReportar.getParent()).addView(btnEliminarPublicacion, ((android.view.ViewGroup) btnReportar.getParent()).indexOfChild(btnReportar) + 1);
            btnEliminarPublicacion.setOnClickListener(v -> eliminarPublicacion());
        }

        adaptadorComentarios = new com.dam.studyfiles.adapters.AdaptadorComentarios(listaComentarios, myId, new com.dam.studyfiles.adapters.AdaptadorComentarios.OnComentarioActionListener() {
            @Override
            public void onEditar(com.dam.studyfiles.models.Comentario c) {
                editarComentario(c);
            }

            @Override
            public void onEliminar(com.dam.studyfiles.models.Comentario c) {
                eliminarComentario(c);
            }
        });
        
        rvComentarios.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        rvComentarios.setAdapter(adaptadorComentarios);

        if (com.dam.studyfiles.utils.DispositivoUtils.isLogged(this)) {
            llComentarInput.setVisibility(View.VISIBLE);
            tvAvisoLoginComentario.setVisibility(View.GONE);
            
            btnEnviarComentario.setOnClickListener(v -> enviarComentario());
        } else {
            llComentarInput.setVisibility(View.GONE);
            tvAvisoLoginComentario.setVisibility(View.VISIBLE);
        }

        cargarComentarios();
    }

    private void cargarComentarios() {
        com.dam.studyfiles.network.SupabaseClient.getApi()
                .getComentarios("eq." + archivoId)
                .enqueue(new Callback<java.util.List<com.dam.studyfiles.models.Comentario>>() {
                    @Override
                    public void onResponse(Call<java.util.List<com.dam.studyfiles.models.Comentario>> call, Response<java.util.List<com.dam.studyfiles.models.Comentario>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            adaptadorComentarios.actualizar(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<java.util.List<com.dam.studyfiles.models.Comentario>> call, Throwable t) {
                        // Error silencioso al cargar comentarios
                    }
                });
    }

    private void enviarComentario() {
        String texto = etNuevoComentario.getText().toString().trim();
        if (texto.isEmpty()) return;

        btnEnviarComentario.setEnabled(false);
        String nombreUsuario = com.dam.studyfiles.utils.DispositivoUtils.getUsuarioNombre(this);
        String usuarioId     = com.dam.studyfiles.utils.DispositivoUtils.getUsuarioId(this);
        String uuid          = com.dam.studyfiles.utils.DispositivoUtils.getUUID(this);

        // Campos exactos de la tabla comentarios en Supabase
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("publicacion_id", (long) archivoId);
        
        // Restauramos el usuario_id con el prefijo estándar del proyecto
        String myId = usuarioId != null ? "user_" + usuarioId : uuid;
        body.put("usuario_id",     myId);
        body.put("usuario_nombre", nombreUsuario != null ? nombreUsuario : "Anónimo");
        body.put("contenido",      texto);
        // Eliminamos 'fecha' para que la base de datos use su valor DEFAULT (now())
        // Esto suele evitar conflictos de duplicidad en milisegundos.

        com.dam.studyfiles.network.SupabaseClient.getApi().crearComentario(body)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        btnEnviarComentario.setEnabled(true);
                        if (response.isSuccessful()) {
                            etNuevoComentario.setText("");
                            cargarComentarios();
                            Toast.makeText(ActividadDetalle.this, "Comentario publicado ✅", Toast.LENGTH_SHORT).show();
                        } else {
                            String errDetail = "Conflicto en la base de datos";
                            try {
                                if (response.errorBody() != null) {
                                    errDetail = response.errorBody().string();
                                }
                            } catch (Exception ignored) {}
                            android.util.Log.e("COMENTARIO_ERROR", "Error: " + errDetail);
                            Toast.makeText(ActividadDetalle.this, "Error: " + errDetail, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        btnEnviarComentario.setEnabled(true);
                        Toast.makeText(ActividadDetalle.this, "Sin conexión", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void eliminarComentario(com.dam.studyfiles.models.Comentario c) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar comentario")
                .setMessage("¿Estás seguro de que quieres borrar este comentario?")
                .setPositiveButton("Borrar", (dialog, which) -> {
                    com.dam.studyfiles.network.SupabaseClient.getApi().eliminarComentario("eq." + c.id)
                            .enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    cargarComentarios();
                                }
                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {}
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void editarComentario(com.dam.studyfiles.models.Comentario c) {
        android.widget.EditText et = new android.widget.EditText(this);
        et.setText(c.contenido);
        
        new AlertDialog.Builder(this)
                .setTitle("Editar comentario")
                .setView(et)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nuevoTexto = et.getText().toString().trim();
                    if (nuevoTexto.isEmpty() || nuevoTexto.equals(c.contenido)) return;
                    
                    java.util.Map<String, Object> campos = new java.util.HashMap<>();
                    campos.put("contenido", nuevoTexto);
                    
                    com.dam.studyfiles.network.SupabaseClient.getApi().actualizarComentario("eq." + c.id, campos)
                            .enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    cargarComentarios();
                                }
                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {}
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarPublicacion() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Publicación")
                .setMessage("¿Seguro que quieres borrar este archivo público? Se perderá para todos.")
                .setPositiveButton("Borrar", (dialog, which) -> {
                    com.dam.studyfiles.network.SupabaseClient.getApi().eliminarComentariosDePublicacion("eq." + archivoId)
                            .enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    if (response.isSuccessful()) {
                                        eliminarArchivoReal();
                                    } else {
                                        Toast.makeText(ActividadDetalle.this, "Error al eliminar comentarios: " + response.code(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    Toast.makeText(ActividadDetalle.this, "Error de red al eliminar comentarios", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarArchivoReal() {
        com.dam.studyfiles.network.SupabaseClient.getApi().eliminarArchivo("eq." + archivoId)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            limpiarFavoritoLocal(); // Quitar de favoritos locales
                            Toast.makeText(ActividadDetalle.this, "Publicación eliminada", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(ActividadDetalle.this, "Error al eliminar archivo: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(ActividadDetalle.this, "Error al eliminar", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /** Elimina el favorito de Room y su archivo local si existía */
    private void limpiarFavoritoLocal() {
        Executors.newSingleThreadExecutor().execute(() -> {
            // Borrar de favoritos Room
            db.favoritoDao().eliminarPorId(archivoId);
            // Borrar archivo descargado localmente si existe
            if (tipoArchivo != null) {
                java.io.File f = new java.io.File(
                        new java.io.File(getFilesDir(), "favoritos"),
                        "fav_" + archivoId + "." + tipoArchivo);
                if (f.exists()) f.delete();
            }
        });
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
                        // Eliminación ahora depende solo de reportes, no de dislikes
                    }
                    @Override public void onFailure(Call<Void> c, Throwable t) {
                        Toast.makeText(ActividadDetalle.this,
                                "Error al guardar voto", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void eliminarPorMalosDislikes() {
        SupabaseClient.getApi().eliminarComentariosDePublicacion("eq." + archivoId)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            eliminarArchivoPorReportesReal();
                        } else {
                            android.util.Log.e("ELIMINAR_POR_REPORTES", "Error al borrar comentarios: " + response.code());
                            eliminarArchivoPorReportesReal();
                        }
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        eliminarArchivoPorReportesReal();
                    }
                });
    }

    private void eliminarArchivoPorReportesReal() {
        SupabaseClient.getApi().eliminarArchivo("eq." + archivoId)
                .enqueue(new Callback<Void>() {
                    @Override public void onResponse(Call<Void> c, Response<Void> r) {
                        if (r.isSuccessful()) {
                            limpiarFavoritoLocal();
                            Toast.makeText(ActividadDetalle.this,
                                    "⚠️ Archivo eliminado por exceso de reportes de la comunidad.",
                                    Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            android.util.Log.e("ELIMINAR_POR_REPORTES", "Error al borrar archivo: " + r.code());
                        }
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
            
            // Intentamos inferir la extensión real si viene como bin
            String extReal = tipoArchivo;
            if (extReal == null || extReal.equals("bin")) {
                extReal = com.dam.studyfiles.utils.DispositivoUtils.obtenerExtension(urlArchivo);
            }
            
            String mime = com.dam.studyfiles.utils.DispositivoUtils.getMimeType(extReal);
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

                // Intentamos sacar la extensión real de la URL si el tipoArchivo es bin o null
                String extParaArchivo = tipoArchivo;
                if (extParaArchivo == null || extParaArchivo.equals("bin")) {
                    extParaArchivo = com.dam.studyfiles.utils.DispositivoUtils.obtenerExtension(urlArchivo);
                }

                String extSuffix = (extParaArchivo != null && !extParaArchivo.equals("bin")) ? "." + extParaArchivo : "";
                File   dest = new File(dir, "fav_" + archivoId + extSuffix);

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
                    fav.institucion = institucion;
                    fav.nivelEstudios = nivelEstudios;
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
        // Probamos con la extensión declarada
        String extDeclarada = tipoArchivo != null && !tipoArchivo.equals("bin") ? "." + tipoArchivo : "";
        File f1 = new File(new File(getFilesDir(), "favoritos"), "fav_" + archivoId + extDeclarada);
        if (f1.exists()) return f1;

        // Si no existe, probamos deduciendo de la URL (por si se descargó así)
        String extUrl = com.dam.studyfiles.utils.DispositivoUtils.obtenerExtension(urlArchivo);
        if (!extUrl.equals("bin")) {
            File f2 = new File(new File(getFilesDir(), "favoritos"), "fav_" + archivoId + "." + extUrl);
            if (f2.exists()) return f2;
        }

        // Por último, probamos sin extensión (formato antiguo)
        File f3 = new File(new File(getFilesDir(), "favoritos"), "fav_" + archivoId);
        if (f3.exists()) return f3;

        return null;
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
                        if (reportes >= 20) {
                            // Auto-eliminar: demasiados reportes de la comunidad
                            eliminarPorMalosDislikes();
                        } else {
                            Toast.makeText(ActividadDetalle.this,
                                    "Archivo reportado. Gracias.", Toast.LENGTH_SHORT).show();
                            btnReportar.setEnabled(false);
                        }
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
