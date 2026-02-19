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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_asignaturas);

        // Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Init
        db = BaseDatosApp.getInstance(getApplicationContext());
        executorService = Executors.newSingleThreadExecutor();
        
        // Get Intent
        especialidadId = getIntent().getIntExtra("especialidad_id", -1);
        if (especialidadId == -1) {
            Toast.makeText(this, "Especialidad no válida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerViewSubjects);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load Data
        loadSubjects();
    }

    private void loadSubjects() {
        executorService.execute(() -> {
            List<Asignatura> lista = db.asignaturaDao().obtenerPorEspecialidad(especialidadId);
            runOnUiThread(() -> {
                AdaptadorAsignaturas adapter = new AdaptadorAsignaturas(lista, asignatura -> {
                    // Click -> Ir a Publicaciones de esta Asignatura
                    Intent intent = new Intent(ActividadAsignaturas.this, ActividadPublicaciones.class);
                    intent.putExtra("asignatura_id", asignatura.id);
                    intent.putExtra("nombre_asignatura", asignatura.nombre);
                    startActivity(intent);
                });
                recyclerView.setAdapter(adapter);
                
                if (lista.isEmpty()) {
                    Toast.makeText(this, "No hay asignaturas registradas", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
