package com.dam.studybro.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.dam.studybro.R;
import com.dam.studybro.adapters.AdaptadorAsignaturas;
import com.dam.studybro.database.BaseDatosApp;
import com.dam.studybro.database.Asignatura;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActividadAsignaturas extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BaseDatosApp db;
    private ExecutorService executorService;
    private int especialidadId;
    private int centroId; // Recibimos y reenviamos el centro
    private com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton fabSugerir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_asignaturas);

        // Configurar Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Inicializar
        db = BaseDatosApp.getInstance(getApplicationContext());
        executorService = Executors.newSingleThreadExecutor();

        // Recibir datos del Intent
        especialidadId = getIntent().getIntExtra("especialidad_id", -1);
        centroId = getIntent().getIntExtra("centro_id", -1);

        if (especialidadId == -1) {
            Toast.makeText(this, "Especialidad no válida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Configurar lista
        recyclerView = findViewById(R.id.recyclerViewSubjects);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Configurar FAB
        fabSugerir = findViewById(R.id.fabSugerir);
        
        android.content.SharedPreferences prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        boolean sesionIniciada = prefs.getBoolean("sesion_iniciada", false);
        if (!sesionIniciada || "ADMIN".equals(prefs.getString("rol_usuario", ""))) {
            fabSugerir.setVisibility(android.view.View.GONE);
        } else {
            fabSugerir.setOnClickListener(v -> mostrarSugerencias());
        }

        cargarAsignaturas();
    }

    private void mostrarSugerencias() {
        android.content.SharedPreferences prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        boolean sesionIniciada = prefs.getBoolean("sesion_iniciada", false);
        if (!sesionIniciada) {
            Toast.makeText(this, "Inicia sesión para proponer nuevas materias", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(this, ActividadSugerirEntidad.class);
        intent.putExtra("centro_id", centroId);
        intent.putExtra("especialidad_id", especialidadId);
        startActivity(intent);
    }

    private void cargarAsignaturas() {
        executorService.execute(() -> {
            List<Asignatura> lista = db.asignaturaDao().obtenerPorEspecialidad(especialidadId);
            runOnUiThread(() -> {
                AdaptadorAsignaturas adapter = new AdaptadorAsignaturas(lista, asignatura -> {
                    // Ir a Publicaciones de esta Asignatura (con centro_id)
                    Intent intent = new Intent(ActividadAsignaturas.this, ActividadPublicaciones.class);
                    intent.putExtra("asignatura_id", asignatura.id);
                    intent.putExtra("nombre_asignatura", asignatura.nombre);
                    intent.putExtra("centro_id", centroId);
                    startActivity(intent);
                });
                recyclerView.setAdapter(adapter);

                if (lista.isEmpty()) {
                    Toast.makeText(this, "No hay asignaturas para esta especialidad", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
