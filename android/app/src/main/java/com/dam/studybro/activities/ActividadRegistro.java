package com.dam.studybro.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.dam.studybro.R;
import com.dam.studybro.supabase.ClienteSupabase;
import com.dam.studybro.supabase.ServicioAuth;
import com.google.android.material.textfield.TextInputEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Registro usando Supabase Auth. Crea la cuenta en la nube.
 */
public class ActividadRegistro extends AppCompatActivity {

    private TextInputEditText campoNombre, campoCorreo, campoContrasena;
    private Button botonRegistrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_registro);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Crear cuenta");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        campoNombre    = findViewById(R.id.etNombreUsuario);
        campoCorreo    = findViewById(R.id.etCorreoRegistro);
        campoContrasena = findViewById(R.id.etContrasenaRegistro);
        botonRegistrar = findViewById(R.id.btnRegistrar);

        botonRegistrar.setOnClickListener(v -> registrarUsuario());
    }

    private void registrarUsuario() {
        String nombre    = campoNombre.getText().toString().trim();
        String correo    = campoCorreo.getText().toString().trim();
        String contrasena = campoContrasena.getText().toString();

        if (nombre.isEmpty() || correo.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }
        if (contrasena.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        botonRegistrar.setEnabled(false);
        botonRegistrar.setText("Creando cuenta...");

        // Llamada a Supabase Auth → signup
        ServicioAuth auth = ClienteSupabase.getAuth();
        Call<ServicioAuth.RespuestaAuth> llamada = auth.registrar(
                new ServicioAuth.PeticionAuth(correo, contrasena));

        llamada.enqueue(new Callback<ServicioAuth.RespuestaAuth>() {
            @Override
            public void onResponse(Call<ServicioAuth.RespuestaAuth> call,
                                   Response<ServicioAuth.RespuestaAuth> response) {
                botonRegistrar.setEnabled(true);
                botonRegistrar.setText("Crear cuenta");

                if (response.isSuccessful()) {
                    // Guardar usuario en Room localmente además de Supabase antes de ir al login
                    java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
                        try {
                            com.dam.studybro.database.BaseDatosApp db = com.dam.studybro.database.BaseDatosApp.getInstance(getApplicationContext());
                            String rolAsignado = correo.equalsIgnoreCase("admin@studybro.com") ? "ADMIN" : "ESTUDIANTE";
                            com.dam.studybro.database.Usuario nuevo = new com.dam.studybro.database.Usuario(nombre, correo, "", rolAsignado);
                            nuevo.id = response.body().usuario.id; // Asignar UUID de Supabase
                            
                            if (db.usuarioDao().buscarPorCorreo(correo) == null) {
                                db.usuarioDao().insertarUsuario(nuevo);
                            }
                        } catch (Exception e) {
                            android.util.Log.e("REGISTRO_LOCAL", "Error guardando: " + e.getMessage());
                        }
                    });

                    Toast.makeText(ActividadRegistro.this,
                            "¡Cuenta creada! Ya puedes iniciar sesión.", Toast.LENGTH_LONG).show();
                    // Volver al login
                    startActivity(new Intent(ActividadRegistro.this, ActividadLogin.class));
                    finish();
                } else {
                    // P.ej: email ya registrado
                    Toast.makeText(ActividadRegistro.this,
                            "Error al registrar. ¿Ya tienes cuenta con ese email?",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ServicioAuth.RespuestaAuth> call, Throwable t) {
                botonRegistrar.setEnabled(true);
                botonRegistrar.setText("Crear cuenta");
                Toast.makeText(ActividadRegistro.this,
                        "Sin conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
