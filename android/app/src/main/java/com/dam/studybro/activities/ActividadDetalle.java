package com.dam.studybro.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dam.studybro.R;
import com.dam.studybro.adapters.AdaptadorComentarios;
import com.dam.studybro.database.BaseDatosApp;
import com.dam.studybro.database.Comentario;
import com.dam.studybro.database.Interaccion;
import com.dam.studybro.database.Publicacion;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Detalle de publicación: muestra título, descripción, autor, chips de asignatura y tipo,
 * botones útil/favorito en la cabecera (no mezclados con los comentarios), y sección de comentarios.
 *
 * Si la publicación es del usuario actual, aparece un menú con opciones Editar y Eliminar.
 * Los invitados no pueden comentar, dar like ni marcar favorito.
 */
public class ActividadDetalle extends AppCompatActivity {

    public static final String EXTRA_PUBLICACION_ID = "publicacion_id";

    private BaseDatosApp db;
    private ExecutorService executor;
    private AdaptadorComentarios adaptadorComentarios;

    private Publicacion publicacion;
    private String emailUsuario;
    private String rolUsuario;
    private boolean sesionIniciada;

    // Vistas
    private MaterialButton btnUtil, btnFavorito;
    private TextView tvContadorUtil, tvContadorFav;
    
    // Hilos de comentarios
    private Integer parentIdSelected = null;
    private View layoutReplyingTo;
    private TextView tvReplyingTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.actividad_detalle);

            db       = BaseDatosApp.getInstance(getApplicationContext());
            executor = Executors.newSingleThreadExecutor();

            SharedPreferences prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
            sesionIniciada = prefs.getBoolean("sesion_iniciada", false);
            emailUsuario   = prefs.getString("email_usuario", "");
            rolUsuario     = prefs.getString("rol_usuario", "ESTUDIANTE");

            int publicacionId = getIntent().getIntExtra(EXTRA_PUBLICACION_ID, -1);
            if (publicacionId == -1) { finish(); return; }

            // Toolbar
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Detalle");
            }

            // Vistas de interacción
            btnUtil          = findViewById(R.id.btnUseful);
            btnFavorito      = findViewById(R.id.btnFavorite);
            tvContadorUtil   = findViewById(R.id.tvContadorUtil);
            tvContadorFav    = findViewById(R.id.tvContadorFav);

            // RecyclerView comentarios
            RecyclerView rv = findViewById(R.id.recyclerComments);
            rv.setLayoutManager(new LinearLayoutManager(this));
            adaptadorComentarios = new AdaptadorComentarios(new ArrayList<>(), emailUsuario, parent -> {
                // Al pulsar responder
                parentIdSelected = parent.id;
                layoutReplyingTo.setVisibility(View.VISIBLE);
                tvReplyingTo.setText("Respondiendo a " + parent.usuarioId);
                findViewById(R.id.etComment).requestFocus();
            }, new AdaptadorComentarios.OnCommentActionListener() {
                @Override
                public void onEditClick(Comentario c) {
                    mostrarDialogoEditarComentario(c);
                }

                @Override
                public void onDeleteClick(Comentario c) {
                    mostrarDialogoEliminarComentario(c);
                }
            });
            rv.setAdapter(adaptadorComentarios);

            // Enviar comentario
            TextInputEditText etComentario  = findViewById(R.id.etComment);
            View              btnEnviar     = findViewById(R.id.btnSendComment);
            if (!sesionIniciada) {
                etComentario.setEnabled(false);
                etComentario.setHint("Inicia sesión para comentar");
                btnEnviar.setEnabled(false);
                btnUtil.setEnabled(false);
                btnFavorito.setEnabled(false);
            } else {
                btnEnviar.setOnClickListener(v -> {
                    String texto = etComentario.getText() != null ? etComentario.getText().toString().trim() : "";
                    if (!texto.isEmpty()) {
                        enviarComentario(texto, etComentario, publicacionId, parentIdSelected);
                    }
                });

                // Configurar cancelación de respuesta
                layoutReplyingTo = findViewById(R.id.layoutReplyingTo);
                tvReplyingTo     = findViewById(R.id.tvReplyingTo);
                findViewById(R.id.btnCancelReply).setOnClickListener(v -> {
                    parentIdSelected = null;
                    layoutReplyingTo.setVisibility(View.GONE);
                });
            }

            // Integración de Gemini AI
            com.google.android.material.floatingactionbutton.FloatingActionButton fabGemini = findViewById(R.id.fabGemini);
            ProgressBar progressBar = findViewById(R.id.progressBar);

            fabGemini.setOnClickListener(v -> {
                if (publicacion == null) return;
                
                // Mostrar menú de opciones
                androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(this, fabGemini);
                popup.getMenu().add(0, 1, 0, "📝 Resumir contenido");
                popup.getMenu().add(0, 2, 1, "💡 Explicar como a un niño");
                popup.getMenu().add(0, 3, 2, "❓ Generar test de repaso");
                
                popup.setOnMenuItemClickListener(item -> {
                    String tipo = "RESUMEN";
                    if (item.getItemId() == 2) tipo = "EXPLICAR";
                    if (item.getItemId() == 3) tipo = "QUIZ";

                    progressBar.setVisibility(View.VISIBLE);
                    fabGemini.setEnabled(false);

                    com.dam.studybro.utils.GeminiHelper.pedirAyudaEspecializada(
                            publicacion.titulo != null ? publicacion.titulo : "Sin Título",
                            publicacion.descripcion != null ? publicacion.descripcion : "",
                            tipo,
                            new com.dam.studybro.utils.GeminiHelper.GeminiCallback() {
                                @Override
                                public void onSuccess(String result) {
                                    progressBar.setVisibility(View.GONE);
                                    fabGemini.setEnabled(true);
                                    mostrarDialogoResumen(result);
                                }

                                @Override
                                public void onError(String error) {
                                    progressBar.setVisibility(View.GONE);
                                    fabGemini.setEnabled(true);
                                    Toast.makeText(ActividadDetalle.this, error, Toast.LENGTH_LONG).show();
                                }
                            }
                    );
                    return true;
                });
                popup.show();
            });

            // Cargar datos de la publicación
            cargarPublicacion(publicacionId);
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error fatal al abrir: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void cargarPublicacion(int publicacionId) {
        com.dam.studybro.network.SupabaseApi api = com.dam.studybro.network.SupabaseClient.getClient().create(com.dam.studybro.network.SupabaseApi.class);
        
        api.getPublicacionPorId("eq." + publicacionId).enqueue(new retrofit2.Callback<List<Publicacion>>() {
            @Override
            public void onResponse(retrofit2.Call<List<Publicacion>> call, retrofit2.Response<List<Publicacion>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    publicacion = response.body().get(0);
                    
                    // Ahora cargamos los comentarios
                    api.getComentarios("eq." + publicacionId).enqueue(new retrofit2.Callback<List<Comentario>>() {
                        @Override
                        public void onResponse(retrofit2.Call<List<Comentario>> call2, retrofit2.Response<List<Comentario>> response2) {
                            List<Comentario> comentariosNube = new java.util.ArrayList<>();
                            if (response2.isSuccessful() && response2.body() != null) {
                                comentariosNube.addAll(response2.body());
                            }
                            
                            // Datos mixtos: Asignaturas e Interacciones siguen en local de momento
                            executor.execute(() -> {
                                com.dam.studybro.database.Asignatura tempAsig = null;
                                if (publicacion.asignaturaId > 0) {
                                    tempAsig = db.asignaturaDao().obtenerPorId(publicacion.asignaturaId);
                                }
                                final com.dam.studybro.database.Asignatura asig = tempAsig;

                                int likes = db.interaccionDao().contarInteracciones(publicacionId, "ME_GUSTA");
                                int favs  = db.interaccionDao().contarInteracciones(publicacionId, "GUARDADO");
                                boolean yaLike = sesionIniciada && db.interaccionDao().obtenerInteraccion(publicacionId, emailUsuario, "ME_GUSTA") != null;
                                boolean yaFav  = sesionIniciada && db.interaccionDao().obtenerInteraccion(publicacionId, emailUsuario, "GUARDADO") != null;

                                List<Comentario> listaOrdenada = organizarComentarios(comentariosNube);

                                runOnUiThread(() -> {
                                    poblarVistas(publicacion, asig, likes, favs, yaLike, yaFav);
                                    adaptadorComentarios.actualizarDatos(listaOrdenada);
                                    
                                    // EMPTY STATE de comentarios
                                    TextView tvNoComments = findViewById(R.id.tvNoComments);
                                    if (tvNoComments != null) {
                                        tvNoComments.setVisibility(listaOrdenada.isEmpty() ? View.VISIBLE : View.GONE);
                                    }

                                    invalidateOptionsMenu(); // Para mostrar/ocultar menú de edición
                                });
                            });
                        }

                        @Override
                        public void onFailure(retrofit2.Call<List<Comentario>> call2, Throwable t) {
                            Toast.makeText(ActividadDetalle.this, "Error cargando comentarios", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    if (publicacion == null) {
                        Toast.makeText(ActividadDetalle.this, "Publicación eliminada o no encontrada", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(ActividadDetalle.this, "No se pudo actualizar la publicación", Toast.LENGTH_SHORT).show();
                        // No cerramos si ya teníamos datos
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<Publicacion>> call, Throwable t) {
                if (publicacion == null) {
                    Toast.makeText(ActividadDetalle.this, "Error de red al cargar la publicación", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ActividadDetalle.this, "Error de red: no se pudo refrescar", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void poblarVistas(Publicacion pub,
                              com.dam.studybro.database.Asignatura asig,
                              int likes, int favs,
                              boolean yaLike, boolean yaFav) {
        // Chips
        if (asig != null) {
            Chip chipSub = findViewById(R.id.chipSubject);
            if (chipSub != null) chipSub.setText(asig.nombre != null ? asig.nombre : "Materia");
            Chip chipCourse = findViewById(R.id.chipCourse);
            // Mostrar curso (1º/2º) si está disponible, si no fallback a lo que diga la asignatura
            String cursoTexto = (pub.curso != null && !pub.curso.isEmpty()) ? pub.curso : (asig.curso > 0 ? asig.curso + "º" : "N/A");
            if (chipCourse != null) chipCourse.setText(cursoTexto + " DAM");
        } else {
             Chip chipSub = findViewById(R.id.chipSubject);
             if (chipSub != null) chipSub.setText("General");
             Chip chipCourse = findViewById(R.id.chipCourse);
             if (chipCourse != null) chipCourse.setText((pub.curso != null && !pub.curso.isEmpty()) ? pub.curso + " DAM" : "General");
        }
        Chip chipType = findViewById(R.id.chipType);
        if (chipType != null && pub.tipo != null) chipType.setText(capitalize(pub.tipo));

        // Título y descripción
        ((TextView) findViewById(R.id.tvTitle)).setText(pub.titulo != null ? pub.titulo : "Sin Título");
        ((TextView) findViewById(R.id.tvDescription)).setText(pub.descripcion != null ? pub.descripcion : "Sin descripción");

        // Autor y fecha
        String autor = pub.usuarioId != null && pub.usuarioId.contains("@")
                ? capitalize(pub.usuarioId.substring(0, pub.usuarioId.indexOf("@")))
                : (pub.usuarioId != null ? pub.usuarioId : "Anónimo");
        ((TextView) findViewById(R.id.tvAuthor)).setText(autor);
        String fecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(new Date(pub.fechaSubida));
        ((TextView) findViewById(R.id.tvDate)).setText("Publicado el " + fecha);

        // Contadores
        if (tvContadorUtil   != null) tvContadorUtil.setText(String.valueOf(likes));
        if (tvContadorFav    != null) tvContadorFav.setText(String.valueOf(favs));

        // Estado de los botones de interacción
        actualizarEstadoBoton(btnUtil,     yaLike, "Útil (" + likes + ")", "✓ Útil (" + likes + ")");
        actualizarEstadoBoton(btnFavorito, yaFav,  "Favorito (" + favs + ")", "✓ Favorito (" + favs + ")");

        // Archivo Adjunto
        View layoutAttachment = findViewById(R.id.layoutAttachment);
        MaterialButton btnView = findViewById(R.id.btnViewAttachment);
        if (pub.archivoUrl != null && !pub.archivoUrl.isEmpty()) {
            layoutAttachment.setVisibility(View.VISIBLE);
            btnView.setOnClickListener(v -> {
                try {
                    android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(pub.archivoUrl));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "No hay una aplicación para abrir este archivo", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            layoutAttachment.setVisibility(View.GONE);
        }

        // Listeners de toggle
        if (sesionIniciada) {
            btnUtil.setOnClickListener(v -> toggleInteraccion(pub.id, "ME_GUSTA"));
            btnFavorito.setOnClickListener(v -> toggleInteraccion(pub.id, "GUARDADO"));
        }
    }

    private void actualizarEstadoBoton(MaterialButton btn, boolean activo, String textoInactivo, String textoActivo) {
        if (btn == null) return;
        btn.setText(activo ? textoActivo : textoInactivo);
        btn.setAlpha(activo ? 1.0f : 0.7f);
    }

    private void toggleInteraccion(int publicacionId, String tipo) {
        executor.execute(() -> {
            Interaccion existente = db.interaccionDao().obtenerInteraccion(publicacionId, emailUsuario, tipo);
            if (existente != null) {
                db.interaccionDao().eliminar(existente);
            } else {
                Interaccion nueva = new Interaccion();
                nueva.publicacionId = publicacionId;
                nueva.usuarioId     = emailUsuario;
                nueva.tipo          = tipo;
                db.interaccionDao().insertar(nueva);
            }
            runOnUiThread(() -> cargarPublicacion(publicacionId));
        });
    }

    private void enviarComentario(String texto, TextInputEditText et, int publicacionId, Integer parentId) {
        et.setEnabled(false);
        com.dam.studybro.network.CrearComentarioRequest c = new com.dam.studybro.network.CrearComentarioRequest();
        c.publicacionId = publicacionId;
        c.usuarioId     = emailUsuario;
        c.contenido     = texto;
        c.fecha         = System.currentTimeMillis();
        c.parentId      = parentId;

        com.dam.studybro.network.SupabaseApi api = com.dam.studybro.network.SupabaseClient.getClient().create(com.dam.studybro.network.SupabaseApi.class);
        api.crearComentario(c).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                if (response.isSuccessful()) {
                    et.setText("");
                    et.setEnabled(true);
                    parentIdSelected = null;
                    layoutReplyingTo.setVisibility(View.GONE);
                    cargarPublicacion(publicacionId);
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "sin cuerpo";
                        android.util.Log.e("SUPABASE_ERROR", "Error 400 detalle: " + errorBody);
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(ActividadDetalle.this, "Error al enviar: " + response.code(), Toast.LENGTH_SHORT).show();
                    et.setEnabled(true);
                }
            }
            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                Toast.makeText(ActividadDetalle.this, "Error de red", Toast.LENGTH_SHORT).show();
                et.setEnabled(true);
            }
        });
    }

    /** Ordena los comentarios para que las respuestas aparezcan después de sus padres */
    private List<Comentario> organizarComentarios(List<Comentario> originales) {
        List<Comentario> raices = new ArrayList<>();
        List<Comentario> respuestas = new ArrayList<>();
        
        for (Comentario c : originales) {
            if (c.parentId == null) raices.add(c);
            else respuestas.add(c);
        }
        
        // El query ya viene ordenado por fecha DESC (más recientes arriba)
        // Para un foro, suele ser mejor fecha ASC o raíces DESC y respuestas ASC
        // Vamos a mantener el orden de Room pero intercalando respuestas
        
        List<Comentario> resultado = new ArrayList<>();
        for (Comentario raiz : raices) {
            resultado.add(raiz);
            // Añadir sus respuestas (si las hay)
            for (Comentario r : respuestas) {
                if (r.parentId.equals(raiz.id)) {
                    resultado.add(r);
                }
            }
        }
        return resultado;
    }

    // ── Menú editar / eliminar (solo si es dueño) ─────────────────────────────

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (publicacion != null && sesionIniciada) {
            boolean esPropia = emailUsuario.equals(publicacion.usuarioId);
            boolean esAdmin  = "ADMIN".equals(rolUsuario);

            // Mostrar el menú si es suya o si es administrador superior
            if (esPropia || esAdmin) {
                getMenuInflater().inflate(R.menu.menu_detalle, menu);
                
                // Un admin que NO es dueño solo debería poder Borrar (moderación), no falsificar Ediciones.
                if (esAdmin && !esPropia) {
                    MenuItem itemEditar = menu.findItem(R.id.action_editar);
                    if (itemEditar != null) itemEditar.setVisible(false);
                }
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        if (item.getItemId() == R.id.action_editar) {
            mostrarDialogoEditar();
            return true;
        }
        if (item.getItemId() == R.id.action_eliminar) {
            confirmarEliminar();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void mostrarDialogoEditar() {
        if (publicacion == null) return;
        android.widget.EditText etTitulo = new android.widget.EditText(this);
        etTitulo.setText(publicacion.titulo);
        android.widget.EditText etDesc = new android.widget.EditText(this);
        etDesc.setText(publicacion.descripcion);
        etDesc.setMinLines(3);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 24, 50, 0);
        layout.addView(etTitulo);
        layout.addView(etDesc);

        new AlertDialog.Builder(this)
                .setTitle("Editar publicación")
                .setView(layout)
                .setPositiveButton("Guardar", (d, w) -> {
                    String nuevoTitulo = etTitulo.getText().toString().trim();
                    String nuevaDesc   = etDesc.getText().toString().trim();
                    if (!nuevoTitulo.isEmpty()) {
                        publicacion.titulo      = nuevoTitulo;
                        publicacion.descripcion = nuevaDesc;
                        com.dam.studybro.network.SupabaseApi api = com.dam.studybro.network.SupabaseClient.getClient().create(com.dam.studybro.network.SupabaseApi.class);
                        api.actualizarPublicacion("eq." + publicacion.id, publicacion).enqueue(new retrofit2.Callback<Void>() {
                            @Override
                            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(ActividadDetalle.this, "Publicación actualizada en nube", Toast.LENGTH_SHORT).show();
                                    cargarPublicacion(publicacion.id);
                                }
                            }
                            @Override
                            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                                Toast.makeText(ActividadDetalle.this, "Error de actualización", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmarEliminar() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar publicación")
                .setMessage("¿Seguro que quieres eliminar esta publicación? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (d, w) -> {
                    com.dam.studybro.network.SupabaseApi api = com.dam.studybro.network.SupabaseClient.getClient().create(com.dam.studybro.network.SupabaseApi.class);
                    api.eliminarPublicacion("eq." + publicacion.id).enqueue(new retrofit2.Callback<Void>() {
                        @Override
                        public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(ActividadDetalle.this, "Publicación eliminada", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                        @Override
                        public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                            Toast.makeText(ActividadDetalle.this, "Error de eliminación", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoEditarComentario(com.dam.studybro.database.Comentario c) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Editar comentario");

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setText(c.contenido);
        // padding
        android.widget.FrameLayout container = new android.widget.FrameLayout(this);
        android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = (int) (20 * getResources().getDisplayMetrics().density);
        params.setMargins(margin, 0, margin, 0);
        input.setLayoutParams(params);
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String nuevoTexto = input.getText().toString().trim();
            if (!nuevoTexto.isEmpty() && !nuevoTexto.equals(c.contenido)) {
                com.dam.studybro.network.CrearComentarioRequest req = new com.dam.studybro.network.CrearComentarioRequest();
                req.contenido = nuevoTexto;
                req.fecha = c.fecha;
                req.usuarioId = c.usuarioId;
                req.publicacionId = c.publicacionId;
                req.parentId = c.parentId;

                com.dam.studybro.network.SupabaseApi api = com.dam.studybro.network.SupabaseClient.getClient().create(com.dam.studybro.network.SupabaseApi.class);
                api.actualizarComentario("eq." + c.id, req).enqueue(new retrofit2.Callback<Void>() {
                    @Override
                    public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                        if (response.isSuccessful()) {
                            cargarPublicacion(c.publicacionId);
                            Toast.makeText(ActividadDetalle.this, "Comentario actualizado", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ActividadDetalle.this, "Error al actualizar: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                        Toast.makeText(ActividadDetalle.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void mostrarDialogoEliminarComentario(com.dam.studybro.database.Comentario c) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Eliminar comentario")
                .setMessage("¿Estás seguro de que quieres eliminar este comentario?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    com.dam.studybro.network.SupabaseApi api = com.dam.studybro.network.SupabaseClient.getClient().create(com.dam.studybro.network.SupabaseApi.class);
                    // Como en Supabase el cálculo `contarRespuestas` se haría en BD, vamos a simplificar y borrar físicamente el comentario o parchearlo.
                    // Para ser seguros con la estructura de árbol, mandaremos un PATCH con [Eliminado].
                    com.dam.studybro.network.CrearComentarioRequest req = new com.dam.studybro.network.CrearComentarioRequest();
                    req.contenido = "[Eliminado]";
                    req.fecha = c.fecha;
                    req.usuarioId = c.usuarioId;
                    req.publicacionId = c.publicacionId;
                    req.parentId = c.parentId;

                    api.actualizarComentario("eq." + c.id, req).enqueue(new retrofit2.Callback<Void>() {
                        @Override
                        public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                            if (response.isSuccessful()) {
                                cargarPublicacion(c.publicacionId);
                                Toast.makeText(ActividadDetalle.this, "Comentario eliminado", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ActividadDetalle.this, "Error al eliminar: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                            Toast.makeText(ActividadDetalle.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoResumen(String textoMarkdown) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("💡 Asistente IA (Gemini)");

        // Limpiar un poco el Markdown básico de Gemini para vista TextView clásica
        String textoLimpio = textoMarkdown.replace("**", "").replace("*", "•");

        ScrollView scrollView = new ScrollView(this);
        scrollView.setPadding(40, 40, 40, 40);
        TextView tvResumen = new TextView(this);
        tvResumen.setText(textoLimpio);
        tvResumen.setTextSize(15f);
        tvResumen.setTextColor(getResources().getColor(R.color.text_primary, getTheme()));
        scrollView.addView(tvResumen);

        builder.setView(scrollView);
        builder.setPositiveButton("Cerrar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
