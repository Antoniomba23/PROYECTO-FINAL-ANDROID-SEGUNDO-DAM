package com.dam.studybro.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.dam.studybro.R;

public class ActividadPerfilCentro extends AppCompatActivity {

    // Variables UI
    private TextView tvNombre, tvUbicacion, tvRating, tvDescripcion, tvHorario, tvAccesibilidad;
    private android.widget.ImageView ivImagen;
    private com.google.android.material.button.MaterialButton btnValorar, btnWeb;
    private androidx.recyclerview.widget.RecyclerView rvEspecialidades, rvResenas;
    
    // Variables Datos
    private com.dam.studybro.database.BaseDatosApp db;
    private int centroId;
    private com.dam.studybro.database.Centro centroActual;
    private java.util.concurrent.ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_perfil_centro);

        // 1. Inicializar DB
        db = com.dam.studybro.database.BaseDatosApp.getInstance(getApplicationContext());
        executorService = java.util.concurrent.Executors.newSingleThreadExecutor();

        // 2. Obtener ID del Intent
        centroId = getIntent().getIntExtra("centro_id", -1);
        if (centroId == -1) {
            Toast.makeText(this, "Error al cargar centro", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 3. Vincular Vistas
        tvNombre = findViewById(R.id.tvCenterName);
        tvUbicacion = findViewById(R.id.tvLocation);
        tvRating = findViewById(R.id.tvRating);
        tvDescripcion = findViewById(R.id.tvDescription);
        tvHorario = findViewById(R.id.tvSchedule);
        tvAccesibilidad = findViewById(R.id.tvAccessibility);
        ivImagen = findViewById(R.id.ivCenterImage);
        btnValorar = findViewById(R.id.btnValorar);
        btnWeb = findViewById(R.id.btnWeb);
        rvEspecialidades = findViewById(R.id.recyclerViewSpecialties);
        rvResenas = findViewById(R.id.recyclerViewReviews);

        // Configurar RecyclerViews
        rvEspecialidades.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        rvResenas.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));

        // 4. Cargar Datos Globales
        cargarDatosCentro();
        cargarEspecialidades();
        cargarResenas();

        // 5. Eventos
        btnValorar.setOnClickListener(v -> mostrarDialogoValoracion());
        btnWeb.setOnClickListener(v -> abrirWeb());
    }

    private void cargarDatosCentro() {
        executorService.execute(() -> {
            centroActual = db.centroDao().obtenerPorId(centroId);
            runOnUiThread(() -> {
                if (centroActual != null) {
                    tvNombre.setText(centroActual.nombre);
                    
                    // Combinar dirección y ciudad para la ubicación
                    String ubicacion = (centroActual.direccion != null ? centroActual.direccion : "") + 
                                     (centroActual.ciudad != null ? ", " + centroActual.ciudad : "");
                    tvUbicacion.setText(ubicacion.isEmpty() ? "Ubicación no disponible" : ubicacion);
                    
                    actualizarTextoRating(centroActual.valoracionMedia);

                    // Descripción
                    if (centroActual.descripcion != null && !centroActual.descripcion.isEmpty()) {
                        tvDescripcion.setText(centroActual.descripcion.trim());
                    } else {
                        tvDescripcion.setText("No hay una descripción detallada para este centro.");
                    }

                    // Horario
                    if (centroActual.horario != null && !centroActual.horario.isEmpty()) {
                        tvHorario.setText(centroActual.horario);
                    } else {
                        tvHorario.setText("Horario no disponible");
                    }

                    // Accesibilidad
                    String acc = "Sin datos de accesibilidad";
                    if ("1".equals(centroActual.accesibilidad)) acc = "Accesibilidad: Instalaciones accesibles";
                    else if ("0".equals(centroActual.accesibilidad)) acc = "Accesibilidad: No accesible o sin datos";
                    tvAccesibilidad.setText(acc);

                    // Cargar Imagen con Glide
                    if (centroActual.imagenUrl != null && !centroActual.imagenUrl.isEmpty()) {
                        com.bumptech.glide.Glide.with(this)
                                .load(centroActual.imagenUrl)
                                .placeholder(R.drawable.ic_launcher_foreground) // Fallback
                                .into(ivImagen);
                    }
                }
            });
        });
    }

    private void abrirWeb() {
        if (centroActual != null && centroActual.webUrl != null && !centroActual.webUrl.isEmpty()) {
            android.content.Intent browserIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(centroActual.webUrl));
            startActivity(browserIntent);
        } else {
            Toast.makeText(this, "No hay web disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarEspecialidades() {
        executorService.execute(() -> {
            // 1. Intentar obtener especialidades vinculadas
            java.util.List<com.dam.studybro.database.Especialidad> lista = 
                    db.centroEspecialidadDao().obtenerPorCentro(centroId);
            
            // 2. Si no hay vinculaciones, parseamos la descripción (Lazy Seeder)
            if (lista.isEmpty() && centroActual != null && centroActual.descripcion != null) {
                java.util.List<com.dam.studybro.database.Especialidad> todas = db.especialidadDao().obtenerTodas();
                java.util.List<com.dam.studybro.database.CentroEspecialidad> nuevasRelaciones = new java.util.ArrayList<>();
                String descLower = centroActual.descripcion.toLowerCase();

                for (com.dam.studybro.database.Especialidad esp : todas) {
                    if (descLower.contains(esp.nombre.toLowerCase())) {
                        nuevasRelaciones.add(new com.dam.studybro.database.CentroEspecialidad(centroId, esp.id));
                    }
                }

                if (!nuevasRelaciones.isEmpty()) {
                    db.centroEspecialidadDao().insertarLista(nuevasRelaciones);
                    // Recargar lista vinculada
                    lista = db.centroEspecialidadDao().obtenerPorCentro(centroId);
                }
            }

            final java.util.List<com.dam.studybro.database.Especialidad> listaFinal = lista;
            runOnUiThread(() -> {
                com.dam.studybro.adapters.AdaptadorEspecialidades adp = new com.dam.studybro.adapters.AdaptadorEspecialidades(listaFinal, especialidad -> {
                    // Navegar a las Asignaturas de esta Especialidad
                    android.content.Intent intent = new android.content.Intent(ActividadPerfilCentro.this, ActividadAsignaturas.class);
                    intent.putExtra("especialidad_id", especialidad.id);
                    intent.putExtra("centro_id", centroId); 
                    startActivity(intent);
                });
                rvEspecialidades.setAdapter(adp);
            });
        });
    }

    private void cargarResenas() {
        executorService.execute(() -> {
            java.util.List<com.dam.studybro.database.ValoracionCentro> lista = db.valoracionCentroDao().obtenerPorCentro(centroId);
            runOnUiThread(() -> {
                com.dam.studybro.adapters.AdaptadorResenas adp = new com.dam.studybro.adapters.AdaptadorResenas(lista);
                rvResenas.setAdapter(adp);
            });
        });
    }

    private void actualizarTextoRating(float media) {
        float mediaRedondeada = (float) (Math.round(media * 10.0) / 10.0);
        tvRating.setText("★ " + mediaRedondeada);
    }

    private void mostrarDialogoValoracion() {
        // Diálogo Custom con Valoración + Texto
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Valora este centro");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // Barra de Estrellas (RatingBar)
        // Usamos un Slider o simple TextFields para simplificar si no tenemos RatingBar en layout,
        // pero mejor usar Input simple de texto para puntuación (1-5) y comentario.
        final android.widget.EditText inputPuntos = new android.widget.EditText(this);
        inputPuntos.setHint("Puntuación (1-5)");
        inputPuntos.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(inputPuntos);

        final android.widget.EditText inputComentario = new android.widget.EditText(this);
        inputComentario.setHint("Escribe tu opinión...");
        layout.addView(inputComentario);

        builder.setView(layout);

        builder.setPositiveButton("Enviar", (dialog, which) -> {
            String puntosStr = inputPuntos.getText().toString();
            String comentario = inputComentario.getText().toString();

            if (!puntosStr.isEmpty()) {
                int puntos = Integer.parseInt(puntosStr);
                if (puntos < 1) puntos = 1;
                if (puntos > 5) puntos = 5;
                guardarValoracion(puntos, comentario);
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void guardarValoracion(int puntuacion, String comentario) {
        // Obtener email del usuario (se usa como identificador)
        android.content.SharedPreferences prefs =
                getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        boolean sesionIniciada = prefs.getBoolean("sesion_iniciada", false);
        if (!sesionIniciada) {
            android.widget.Toast.makeText(this, "Inicia sesión para valorar", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        String email = prefs.getString("email_usuario", "anonimo");

        executorService.execute(() -> {
            com.dam.studybro.database.ValoracionCentro nuevaVal = new com.dam.studybro.database.ValoracionCentro(
                    puntuacion, comentario, System.currentTimeMillis(), 0, centroId);
            // Guardamos email en el campo comentario prefijado para identificar al autor
            // (campo usuario_id es int por esquema; usamos 0 como placeholder hasta migración)
            db.valoracionCentroDao().insertar(nuevaVal);

            float nuevaMedia = db.valoracionCentroDao().obtenerMedia(centroId);
            centroActual.valoracionMedia = nuevaMedia;
            db.centroDao().actualizar(centroActual);

            runOnUiThread(() -> {
                actualizarTextoRating(nuevaMedia);
                cargarResenas();
                android.widget.Toast.makeText(this, "Opinión guardada", android.widget.Toast.LENGTH_SHORT).show();
            });
        });
    }
}
