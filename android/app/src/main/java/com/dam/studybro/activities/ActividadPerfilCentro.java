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
    private com.google.android.material.button.MaterialButton btnValorar, btnWeb, btnSugerirEntidad;
    private androidx.recyclerview.widget.RecyclerView rvEspecialidades, rvResenas;
    
    // Variables Datos
    private com.dam.studybro.database.BaseDatosApp db;
    private int centroId;
    private com.dam.studybro.database.Centro centroActual;
    private java.util.concurrent.ExecutorService executorService;
    private String opinionesParaIA = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_perfil_centro);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Perfil del Centro");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

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
        btnValorar = findViewById(R.id.btnValorar);
        btnWeb = findViewById(R.id.btnWeb);
        btnSugerirEntidad = findViewById(R.id.btnSugerirEntidad);
        rvEspecialidades = findViewById(R.id.recyclerViewSpecialties);
        rvResenas = findViewById(R.id.recyclerViewReviews);

        // Configurar RecyclerViews
        rvEspecialidades.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        rvResenas.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));

        // 4. Cargar Datos Globales
        cargarDatosCentro();
        cargarEspecialidades();
        cargarResenas();
        
        // 4b. Ocultar sugerencias y valoración si es admin o invitado
        android.content.SharedPreferences prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        boolean sesionIniciada = prefs.getBoolean("sesion_iniciada", false);
        String rol = prefs.getString("rol_usuario", "");

        if (!sesionIniciada || "ADMIN".equals(rol)) {
            btnSugerirEntidad.setVisibility(android.view.View.GONE);
            btnValorar.setVisibility(android.view.View.GONE);
        }

        // 5. Eventos
        btnValorar.setOnClickListener(v -> mostrarDialogoValoracion());
        btnWeb.setOnClickListener(v -> abrirWeb());
        btnSugerirEntidad.setOnClickListener(v -> mostrarSugerencias());

        // 6. Bot Gemini y Navegación
        com.google.android.material.floatingactionbutton.FloatingActionButton fabGemini = findViewById(R.id.fabGemini);
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        com.dam.studybro.utils.NavigationHelper.setupBottomNavigation(this, bottomNav);

        if (fabGemini != null) {
            fabGemini.setOnClickListener(v -> {
                if (opinionesParaIA == null || opinionesParaIA.isEmpty()) {
                    Toast.makeText(this, "No hay opiniones suficientes para analizar", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                fabGemini.setEnabled(false);
                
                com.dam.studybro.utils.GeminiHelper.analizarOpiniones(
                    centroActual != null ? centroActual.nombre : "este centro",
                    opinionesParaIA,
                    new com.dam.studybro.utils.GeminiHelper.GeminiCallback() {
                        @Override
                        public void onSuccess(String result) {
                            fabGemini.setEnabled(true);
                            mostrarDialogoResumen(result);
                        }

                        @Override
                        public void onError(String error) {
                            fabGemini.setEnabled(true);
                            Toast.makeText(ActividadPerfilCentro.this, error, Toast.LENGTH_LONG).show();
                        }
                    }
                );
            });
        }
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

    private void mostrarSugerencias() {
        android.content.SharedPreferences prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        boolean sesionIniciada = prefs.getBoolean("sesion_iniciada", false);
        if (!sesionIniciada) {
            Toast.makeText(this, "Inicia sesión para poder sugerir asignaturas o especialidades", Toast.LENGTH_LONG).show();
            return;
        }

        android.content.Intent intent = new android.content.Intent(this, ActividadSugerirEntidad.class);
        // Aunque la sugerencia pilla el centroIdActual de sharedprefs, le pasamos por intent por si acaso
        intent.putExtra("centro_id", centroId);
        startActivity(intent);
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
        com.dam.studybro.network.SupabaseApi api = com.dam.studybro.network.SupabaseClient.getClient().create(com.dam.studybro.network.SupabaseApi.class);
        api.getValoracionesCentro("eq." + centroId).enqueue(new retrofit2.Callback<java.util.List<com.dam.studybro.database.ValoracionCentro>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.List<com.dam.studybro.database.ValoracionCentro>> call, retrofit2.Response<java.util.List<com.dam.studybro.database.ValoracionCentro>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    java.util.List<com.dam.studybro.database.ValoracionCentro> lista = response.body();
                    
                    // Calcular nueva media
                    if (!lista.isEmpty()) {
                        float suma = 0;
                        for (com.dam.studybro.database.ValoracionCentro v : lista) {
                            suma += v.puntuacion;
                        }
                        actualizarTextoRating(suma / lista.size());
                    } else {
                        actualizarTextoRating(0);
                    }

                    com.dam.studybro.adapters.AdaptadorResenas adp = new com.dam.studybro.adapters.AdaptadorResenas(lista);
                    rvResenas.setAdapter(adp);

                    // Preparar texto para Gemini
                    StringBuilder sb = new StringBuilder();
                    for (com.dam.studybro.database.ValoracionCentro v : lista) {
                        String comentarioLimpio = v.comentario.contains("|||") ? v.comentario.split("\\|\\|\\|")[1] : v.comentario;
                        sb.append("- (").append(v.puntuacion).append(" estrellas) ").append(comentarioLimpio).append("\n");
                    }
                    opinionesParaIA = sb.toString();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<java.util.List<com.dam.studybro.database.ValoracionCentro>> call, Throwable t) {
                Toast.makeText(ActividadPerfilCentro.this, "Error al cargar reseñas de la nube", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarTextoRating(float media) {
        float mediaRedondeada = (float) (Math.round(media * 10.0) / 10.0);
        tvRating.setText("★ " + mediaRedondeada);
    }

    private void mostrarDialogoValoracion() {
        android.content.SharedPreferences prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        String email = prefs.getString("email_usuario", "");
        
        if (email.isEmpty()) {
            Toast.makeText(this, "Error de sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        // Buscar si ya existe una valoración de este usuario en la nube
        com.dam.studybro.network.SupabaseApi api = com.dam.studybro.network.SupabaseClient.getClient().create(com.dam.studybro.network.SupabaseApi.class);
        
        api.getValoracionPropia("eq." + centroId, "eq." + email).enqueue(new retrofit2.Callback<java.util.List<com.dam.studybro.database.ValoracionCentro>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.List<com.dam.studybro.database.ValoracionCentro>> call, retrofit2.Response<java.util.List<com.dam.studybro.database.ValoracionCentro>> response) {
                com.dam.studybro.database.ValoracionCentro valPrevia = null;
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    valPrevia = response.body().get(0);
                }
                
                final com.dam.studybro.database.ValoracionCentro fValPrevia = valPrevia;
                mostrarDialogoRating(fValPrevia, email);
            }

            @Override
            public void onFailure(retrofit2.Call<java.util.List<com.dam.studybro.database.ValoracionCentro>> call, Throwable t) {
                mostrarDialogoRating(null, email);
            }
        });
    }

    private void mostrarDialogoRating(com.dam.studybro.database.ValoracionCentro fValPrevia, String email) {
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialogo_valoracion, null);
        android.widget.RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        com.google.android.material.textfield.TextInputEditText etComentario = dialogView.findViewById(R.id.etComentario);

        if (fValPrevia != null) {
            ratingBar.setRating(fValPrevia.puntuacion);
            String[] partes = fValPrevia.comentario.split("\\|\\|\\|");
            if (partes.length >= 2) {
                etComentario.setText(partes[1]);
            } else {
                etComentario.setText(fValPrevia.comentario);
            }
        }

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ActividadPerfilCentro.this)
                .setTitle(fValPrevia != null ? "Editar tu valoración" : "Valora este centro")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    int puntuacion = (int) ratingBar.getRating();
                    String comentario = etComentario.getText() != null ? etComentario.getText().toString().trim() : "";
                    
                    if (puntuacion > 0) {
                        guardarValoracion(puntuacion, comentario, fValPrevia, email);
                    } else {
                        Toast.makeText(ActividadPerfilCentro.this, "Por favor, selecciona al menos una estrella", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null);
        
        if (fValPrevia != null) {
            builder.setNeutralButton("Borrar", (dialog, which) -> borrarValoracion(fValPrevia));
        }
        
        builder.show();
    }

    private void guardarValoracion(int puntuacion, String comentario, com.dam.studybro.database.ValoracionCentro valPrevia, String email) {
        com.dam.studybro.network.SupabaseApi api = com.dam.studybro.network.SupabaseClient.getClient().create(com.dam.studybro.network.SupabaseApi.class);
        String comentarioAnotado = email + "|||" + comentario;
        
        if (valPrevia != null) {
            // Actualizar existente
            com.dam.studybro.network.CrearValoracionRequest req = new com.dam.studybro.network.CrearValoracionRequest();
            req.puntuacion = puntuacion;
            req.comentario = comentarioAnotado;
            req.fecha = System.currentTimeMillis();
            req.usuarioEmail = email;
            req.centroId = centroId;
            
            api.actualizarValoracionCentro("eq." + valPrevia.id, req).enqueue(new retrofit2.Callback<Void>() {
                @Override
                public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                    if (response.isSuccessful()) {
                        cargarResenas(); // Esto actualizará la media y la lista
                        Toast.makeText(ActividadPerfilCentro.this, "Opinión actualizada", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ActividadPerfilCentro.this, "Error al actualizar: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(retrofit2.Call<java.lang.Void> call, java.lang.Throwable t) {
                    Toast.makeText(ActividadPerfilCentro.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Crear nueva
            com.dam.studybro.network.CrearValoracionRequest nuevaVal = new com.dam.studybro.network.CrearValoracionRequest();
            nuevaVal.puntuacion = puntuacion;
            nuevaVal.comentario = comentarioAnotado;
            nuevaVal.fecha = System.currentTimeMillis();
            nuevaVal.usuarioEmail = email;
            nuevaVal.centroId = centroId;
            
            api.crearValoracionCentro(nuevaVal).enqueue(new retrofit2.Callback<Void>() {
                @Override
                public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                    if (response.isSuccessful()) {
                        cargarResenas();
                        Toast.makeText(ActividadPerfilCentro.this, "Opinión guardada", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                    Toast.makeText(ActividadPerfilCentro.this, "Error al guardar", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private void borrarValoracion(com.dam.studybro.database.ValoracionCentro valPrevia) {
        com.dam.studybro.network.SupabaseApi api = com.dam.studybro.network.SupabaseClient.getClient().create(com.dam.studybro.network.SupabaseApi.class);
        api.eliminarValoracionCentro("eq." + valPrevia.id).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                if (response.isSuccessful()) {
                    cargarResenas();
                    Toast.makeText(ActividadPerfilCentro.this, "Opinión eliminada", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ActividadPerfilCentro.this, "Error al eliminar: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                Toast.makeText(ActividadPerfilCentro.this, "Error al eliminar: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogoResumen(String textoMarkdown) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("💡 Análisis de la IA (Gemini)");

        // Limpiar un poco el Markdown básico de Gemini
        String textoLimpio = textoMarkdown.replace("**", "").replace("*", "•");

        android.widget.ScrollView scrollView = new android.widget.ScrollView(this);
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

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
