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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_publicaciones); // Layout renombrado

        // 1. Configurar RecyclerView (El Restaurante)
        recyclerView = findViewById(R.id.recyclerViewPosts);
        
        // LayoutManager (El Gerente) - Lista Vertical
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 2. Datos (La Comida - Mock de prueba)
        List<Publicacion> listaComida = new ArrayList<>();
        // Publicacion(titulo, descripcion, tipo, fechaSubida, usuarioId, asignaturaId)
        listaComida.add(new Publicacion("Apuntes Java", "Tema 1: Variables", "APUNTE", System.currentTimeMillis(), 1, 1));
        listaComida.add(new Publicacion("Examen Pasado", "Examen 2023 Final", "EXAMEN", System.currentTimeMillis() - 3600000, 2, 3));
        listaComida.add(new Publicacion("Duda Android", "¿Cómo funciona RecyclerView?", "DUDA", System.currentTimeMillis() - 7200000, 3, 2));
        listaComida.add(new Publicacion("Resumen Hilos", "ProcessBuilder y Process", "APUNTE", System.currentTimeMillis() - 86400000, 4, 2));

        // 3. Adaptador (El Camarero)
        adaptador = new AdaptadorPublicaciones(listaComida);
        recyclerView.setAdapter(adaptador);

        Toast.makeText(this, "Cargadas " + listaComida.size() + " publicaciones", Toast.LENGTH_SHORT).show();

        // 4. FAB para Nueva Publicación (Nivel 2: Navegación)
        com.google.android.material.floatingactionbutton.FloatingActionButton fab = findViewById(R.id.fabNewPost);
        fab.setOnClickListener(v -> startActivity(new Intent(this, ActividadNuevaPublicacion.class)));
    }
}
