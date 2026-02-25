package com.dam.studybro.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
    private boolean sesionIniciada;

    // Vistas
    private MaterialButton btnUtil, btnFavorito;
    private TextView tvContadorUtil, tvContadorFav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_detalle);

        db       = BaseDatosApp.getInstance(getApplicationContext());
        executor = Executors.newSingleThreadExecutor();

        SharedPreferences prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        sesionIniciada = prefs.getBoolean("sesion_iniciada", false);
        emailUsuario   = prefs.getString("email_usuario", "");

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
        adaptadorComentarios = new AdaptadorComentarios(new ArrayList<>());
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
                    enviarComentario(texto, etComentario, publicacionId);
                }
            });
        }

        // Cargar datos de la publicación
        cargarPublicacion(publicacionId);
    }

    private void cargarPublicacion(int publicacionId) {
        executor.execute(() -> {
            publicacion = db.publicacionDao().obtenerPorId(publicacionId);
            if (publicacion == null) {
                runOnUiThread(this::finish);
                return;
            }

            // Obtener info de asignatura para los chips
            com.dam.studybro.database.Asignatura asig =
                    db.asignaturaDao().obtenerPorId(publicacion.asignaturaId);

            // Contadores de interacciones
            int likes = db.interaccionDao().contarInteracciones(publicacionId, "ME_GUSTA");
            int favs  = db.interaccionDao().contarInteracciones(publicacionId, "GUARDADO");

            // ¿Ya he interactuado?
            boolean yaLike = sesionIniciada &&
                    db.interaccionDao().obtenerInteraccion(publicacionId, emailUsuario, "ME_GUSTA") != null;
            boolean yaFav  = sesionIniciada &&
                    db.interaccionDao().obtenerInteraccion(publicacionId, emailUsuario, "GUARDADO") != null;

            // Comentarios
            List<Comentario> comentarios = db.comentarioDao().obtenerPorPublicacion(publicacionId);

            runOnUiThread(() -> {
                poblarVistas(publicacion, asig, likes, favs, yaLike, yaFav);
                adaptadorComentarios.actualizarDatos(comentarios);
                invalidateOptionsMenu(); // Para mostrar/ocultar menú de edición
            });
        });
    }

    private void poblarVistas(Publicacion pub,
                              com.dam.studybro.database.Asignatura asig,
                              int likes, int favs,
                              boolean yaLike, boolean yaFav) {
        // Chips
        if (asig != null) {
            Chip chipSub = findViewById(R.id.chipSubject);
            if (chipSub != null) chipSub.setText(asig.nombre);
            Chip chipCourse = findViewById(R.id.chipCourse);
            if (chipCourse != null) chipCourse.setText(asig.curso + "º DAM");
        }
        Chip chipType = findViewById(R.id.chipType);
        if (chipType != null && pub.tipo != null) chipType.setText(capitalize(pub.tipo));

        // Título y descripción
        ((TextView) findViewById(R.id.tvTitle)).setText(pub.titulo);
        ((TextView) findViewById(R.id.tvDescription)).setText(pub.descripcion);

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

        // Estado de los botones
        actualizarEstadoBoton(btnUtil,     yaLike, "Útil (" + likes + ")", "✓ Útil (" + likes + ")");
        actualizarEstadoBoton(btnFavorito, yaFav,  "Favorito (" + favs + ")", "✓ Favorito (" + favs + ")");

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

    private void enviarComentario(String texto, TextInputEditText et, int publicacionId) {
        et.setEnabled(false);
        executor.execute(() -> {
            Comentario c = new Comentario();
            c.publicacionId = publicacionId;
            c.usuarioId     = emailUsuario;
            c.contenido     = texto;
            c.fecha         = System.currentTimeMillis();
            db.comentarioDao().insertar(c);
            runOnUiThread(() -> {
                et.setText("");
                et.setEnabled(true);
                cargarPublicacion(publicacionId);
            });
        });
    }

    // ── Menú editar / eliminar (solo si es dueño) ─────────────────────────────

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (publicacion != null && sesionIniciada
                && emailUsuario.equals(publicacion.usuarioId)) {
            getMenuInflater().inflate(R.menu.menu_detalle, menu);
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
                        executor.execute(() -> {
                            publicacion.titulo      = nuevoTitulo;
                            publicacion.descripcion = nuevaDesc;
                            db.publicacionDao().actualizar(publicacion);
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Publicación actualizada", Toast.LENGTH_SHORT).show();
                                cargarPublicacion(publicacion.id);
                            });
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
                    executor.execute(() -> {
                        db.publicacionDao().eliminar(publicacion);
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Publicación eliminada", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
