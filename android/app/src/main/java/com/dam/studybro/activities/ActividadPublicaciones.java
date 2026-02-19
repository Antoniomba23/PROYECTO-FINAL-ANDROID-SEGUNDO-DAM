package com.dam.studybro.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.dam.studybro.R;
import com.dam.studybro.adapters.AdaptadorPublicaciones;
import com.dam.studybro.database.Publicacion;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity de Publicaciones - Nivel 3: Listas Infinitas
 * Responsable: Antonio
 */
public class ActividadPublicaciones extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdaptadorPublicaciones adaptador;
    private com.dam.studybro.database.BaseDatosApp db;
    private java.util.concurrent.ExecutorService executorService;
    private int asignaturaId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_publicaciones);

        // Toolbar
        getSupportActionBar().setTitle("Publicaciones");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // 1. Init DB
        db = com.dam.studybro.database.BaseDatosApp.getInstance(getApplicationContext());
        executorService = java.util.concurrent.Executors.newSingleThreadExecutor();

        // 2. Intent Data
        asignaturaId = getIntent().getIntExtra("asignatura_id", -1);
        String nombreAsignatura = getIntent().getStringExtra("nombre_asignatura");
        if (nombreAsignatura != null) {
            getSupportActionBar().setSubtitle(nombreAsignatura);
        }

        // 3. Configurar RecyclerView
        recyclerView = findViewById(R.id.recyclerViewPosts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 4. Cargar Datos
        cargarPublicaciones();

        // 5. FAB
        com.google.android.material.floatingactionbutton.FloatingActionButton fab = findViewById(R.id.fabNewPost);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, ActividadNuevaPublicacion.class);
            if (asignaturaId != -1) {
                intent.putExtra("asignatura_id_preselected", asignaturaId);
            }
            startActivity(intent);
        });
    }

    private void cargarPublicaciones() {
        executorService.execute(() -> {
            List<Publicacion> lista;
            if (asignaturaId != -1) {
                lista = db.publicacionDao().obtenerPorAsignatura(asignaturaId);
            } else {
                lista = db.publicacionDao().obtenerTodas();
            }

            runOnUiThread(() -> {
                adaptador = new AdaptadorPublicaciones(lista);
                recyclerView.setAdapter(adaptador);
                
                if (lista.isEmpty()) {
                    Toast.makeText(this, "No hay publicaciones aún", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Cargadas " + lista.size() + " publicaciones", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarPublicaciones(); // Recargar al volver de "Nueva Publicación"
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
