package com.dam.studybro.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.dam.studybro.R;

public class ActividadPerfilCentro extends AppCompatActivity {

    // Variables UI
    private TextView tvNombre, tvUbicacion, tvRating;
    private android.widget.Button btnValorar;
    
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
        btnValorar = findViewById(R.id.btnValorar);

        // 4. Cargar Datos del Centro
        cargarDatosCentro();

        // 5. Evento Valorar
        btnValorar.setOnClickListener(v -> mostrarDialogoValoracion());
    }

    private void cargarDatosCentro() {
        executorService.execute(() -> {
            centroActual = db.centroDao().obtenerPorId(centroId);
            runOnUiThread(() -> {
                if (centroActual != null) {
                    tvNombre.setText(centroActual.nombre);
                    tvUbicacion.setText(centroActual.ciudad != null ? centroActual.ciudad : "Ubicación desconocida");
                    actualizarTextoRating(centroActual.valoracionMedia);
                }
            });
        });
    }

    private void actualizarTextoRating(float media) {
        // Redondear a 1 decimal
        float mediaRedondeada = (float) (Math.round(media * 10.0) / 10.0);
        tvRating.setText("★ " + mediaRedondeada);
    }

    private void mostrarDialogoValoracion() {
        // Simple diálogo con opciones 1-5
        String[] opciones = {"1 ★", "2 ★", "3 ★", "4 ★", "5 ★"};
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Califica este centro")
            .setItems(opciones, (dialog, which) -> {
                int puntuacion = which + 1; // which empieza en 0
                guardarValoracion(puntuacion);
            })
            .show();
    }

    private void guardarValoracion(int puntuacion) {
        executorService.execute(() -> {
            // 1. Insertar valoración (Usuario ID simulado 1 por ahora o leer de prefs)
            // Para simplificar, usamos ID 1. En producción leeríamos de SharedPreferences.
            com.dam.studybro.database.ValoracionCentro nuevaVal = new com.dam.studybro.database.ValoracionCentro(
                    puntuacion, "Valoración rápida", System.currentTimeMillis(), 1, centroId);
            
            db.valoracionCentroDao().insertar(nuevaVal);

            // 2. Recalcular media
            float nuevaMedia = db.valoracionCentroDao().obtenerMedia(centroId);

            // 3. Actualizar Centro
            centroActual.valoracionMedia = nuevaMedia;
            db.centroDao().actualizar(centroActual);

            // 4. Actualizar UI
            runOnUiThread(() -> {
                actualizarTextoRating(nuevaMedia);
                Toast.makeText(this, "¡Gracias! Nueva media: " + nuevaMedia, Toast.LENGTH_SHORT).show();
            });
        });
    }
}
