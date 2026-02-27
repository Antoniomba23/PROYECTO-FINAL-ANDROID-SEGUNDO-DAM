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

        configurarAdaptador();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarUsuariosDesdeBD();
    }

    private void cargarUsuariosDesdeBD() {
        com.dam.studybro.network.SupabaseApi api = com.dam.studybro.network.SupabaseClient.getClient().create(com.dam.studybro.network.SupabaseApi.class);
        api.getUsuarios().enqueue(new retrofit2.Callback<List<Usuario>>() {
            @Override
            public void onResponse(retrofit2.Call<List<Usuario>> call, retrofit2.Response<List<Usuario>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Usuario> lista = response.body();
                    adaptador.actualizarDatos(lista);
                    
                    // Sincronizar localmente (opcional pero recomendado)
                    Executors.newSingleThreadExecutor().execute(() -> {
                        BaseDatosApp.getInstance(getApplicationContext()).usuarioDao().insertarLista(lista);
                    });
                } else {
                    // Si falla la nube, cargar local
                    cargarDesdeLocal();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<Usuario>> call, Throwable t) {
                cargarDesdeLocal();
            }
        });
    }

    private void cargarDesdeLocal() {
        Executors.newSingleThreadExecutor().execute(() -> {
            BaseDatosApp db = BaseDatosApp.getInstance(getApplicationContext());
            List<Usuario> lista = db.usuarioDao().obtenerTodos();
            runOnUiThread(() -> {
                if (lista != null && !lista.isEmpty()) {
                    adaptador.actualizarDatos(lista);
                } else {
                    android.widget.Toast.makeText(ActividadGestionUsuarios.this, "Error al conectar con la nube", android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void configurarAdaptador() {
        adaptador = new AdaptadorUsuarios(new ArrayList<>(), new AdaptadorUsuarios.OnUsuarioActionListener() {
            @Override
            public void onCambiarRol(Usuario usuario) {
                String nuevoRol = "ADMIN".equals(usuario.rol) ? "ESTUDIANTE" : "ADMIN";
                usuario.rol = nuevoRol;
                
                com.dam.studybro.network.SupabaseApi api = com.dam.studybro.network.SupabaseClient.getClient().create(com.dam.studybro.network.SupabaseApi.class);
                api.actualizarUsuario("eq." + usuario.id, usuario).enqueue(new retrofit2.Callback<Void>() {
                    @Override
                    public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                        if (response.isSuccessful()) {
                            android.widget.Toast.makeText(ActividadGestionUsuarios.this, "Rol actualizado para " + usuario.nombre, android.widget.Toast.LENGTH_SHORT).show();
                            cargarUsuariosDesdeBD();
                        }
                    }
                    @Override
                    public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                        android.widget.Toast.makeText(ActividadGestionUsuarios.this, "Error de red", android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onEliminar(Usuario usuario) {
                new androidx.appcompat.app.AlertDialog.Builder(ActividadGestionUsuarios.this)
                    .setTitle("Eliminar Usuario")
                    .setMessage("¿Estás seguro de que deseas eliminar a " + usuario.nombre + "?")
                    .setPositiveButton("Eliminar", (dialog, which) -> {
                        com.dam.studybro.network.SupabaseApi api = com.dam.studybro.network.SupabaseClient.getClient().create(com.dam.studybro.network.SupabaseApi.class);
                        api.eliminarUsuario("eq." + usuario.id).enqueue(new retrofit2.Callback<Void>() {
                            @Override
                            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                                if (response.isSuccessful()) {
                                    android.widget.Toast.makeText(ActividadGestionUsuarios.this, "Usuario eliminado", android.widget.Toast.LENGTH_SHORT).show();
                                    cargarUsuariosDesdeBD();
                                }
                            }
                            @Override
                            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                                android.widget.Toast.makeText(ActividadGestionUsuarios.this, "Error al eliminar", android.widget.Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
            }
        });
        recyclerUsuarios.setAdapter(adaptador);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
