package com.dam.studybro.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dam.studybro.R;
import com.dam.studybro.adapters.AdaptadorUsuarios;
import com.dam.studybro.database.BaseDatosApp;
import com.dam.studybro.database.Usuario;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Actividad exclusiva para el panel de administración
 * Muestra la lista de todos los usuarios registrados localmente en Room.
 */
public class ActividadGestionUsuarios extends AppCompatActivity {

    private RecyclerView recyclerUsuarios;
    private AdaptadorUsuarios adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_gestion_usuarios);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Gestión de Usuarios");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerUsuarios = findViewById(R.id.recyclerUsuarios);
        recyclerUsuarios.setLayoutManager(new LinearLayoutManager(this));

        adaptador = new AdaptadorUsuarios(new ArrayList<>());
        recyclerUsuarios.setAdapter(adaptador);

        cargarUsuariosDesdeBD();
    }

    private void cargarUsuariosDesdeBD() {
        Executors.newSingleThreadExecutor().execute(() -> {
            BaseDatosApp db = BaseDatosApp.getInstance(getApplicationContext());
            List<Usuario> lista = db.usuarioDao().obtenerTodos();

            runOnUiThread(() -> {
                if (lista != null && !lista.isEmpty()) {
                    adaptador.actualizarDatos(lista);
                } else {
                    android.widget.Toast.makeText(ActividadGestionUsuarios.this, "No hay usuarios registrados aún", android.widget.Toast.LENGTH_SHORT).show();
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
