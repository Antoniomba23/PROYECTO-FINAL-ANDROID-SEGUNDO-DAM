package com.dam.studybro.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
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
 * Login usando Supabase Auth (email + contraseña reales en la nube).
 */
public class ActividadLogin extends AppCompatActivity {

    private TextInputEditText campoCorreo, campoContrasena;
    private Button botonEntrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Si ya hay sesión activa, ir directamente al home
        SharedPreferences prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        if (prefs.getBoolean("sesion_iniciada", false)) {
            irAHome();
            return;
        }

        setContentView(R.layout.actividad_login);

        campoCorreo    = findViewById(R.id.etCorreo);
        campoContrasena = findViewById(R.id.etContrasena);
        botonEntrar    = findViewById(R.id.btnEntrar);
        TextView botonIrRegistro = findViewById(R.id.btnIrRegistro);

        botonEntrar.setOnClickListener(v -> hacerLogin());
        botonIrRegistro.setOnClickListener(v ->
                startActivity(new Intent(this, ActividadRegistro.class)));
    }

    private void hacerLogin() {
        String correo    = campoCorreo.getText().toString().trim();
        String contrasena = campoContrasena.getText().toString();

        if (correo.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        botonEntrar.setEnabled(false);
        botonEntrar.setText("Entrando...");

        // Llamada a Supabase Auth
        ServicioAuth auth = ClienteSupabase.getAuth();
        Call<ServicioAuth.RespuestaAuth> llamada = auth.login(
                new ServicioAuth.PeticionAuth(correo, contrasena));

        llamada.enqueue(new Callback<ServicioAuth.RespuestaAuth>() {
            @Override
            public void onResponse(Call<ServicioAuth.RespuestaAuth> call,
                                   Response<ServicioAuth.RespuestaAuth> response) {
                botonEntrar.setEnabled(true);
                botonEntrar.setText("Entrar");

                if (response.isSuccessful() && response.body() != null) {
                    ServicioAuth.RespuestaAuth cuerpo = response.body();
                    String token = cuerpo.accessToken;

                    // Guardar sesión en SharedPreferences
                    SharedPreferences prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("sesion_iniciada", true);
                    editor.putString("email_usuario", correo);
                    editor.putString("supabase_token", token);
                    editor.apply();

                    Toast.makeText(ActividadLogin.this, "¡Bienvenido!", Toast.LENGTH_SHORT).show();

                    // ¿Ya tiene centro asignado? Si no, pedir que seleccione uno
                    boolean tieneCentro = prefs.getInt("centro_id", -1) != -1;
                    if (tieneCentro) {
                        irAHome();
                    } else {
                        Intent intent = new Intent(ActividadLogin.this, ActividadSeleccionarCentro.class);
                        intent.putExtra("primer_setup", true);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    // Credenciales incorrectas u otro error
                    Toast.makeText(ActividadLogin.this,
                            "Email o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ServicioAuth.RespuestaAuth> call, Throwable t) {
                botonEntrar.setEnabled(true);
                botonEntrar.setText("Entrar");
                Toast.makeText(ActividadLogin.this,
                        "Sin conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void irAHome() {
        startActivity(new Intent(this, ActividadPrincipal.class));
        finish();
    }
}
