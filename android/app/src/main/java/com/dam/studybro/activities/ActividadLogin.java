package com.dam.studybro.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.dam.studybro.R;
import com.dam.studybro.utils.Seguridad;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Activity de Login - Pantalla de inicio de sesión
 * Responsable: Antonio
 */
public class ActividadLogin extends AppCompatActivity {
    
    // Variables
    private TextInputEditText campoCorreo;
    private TextInputEditText campoContrasena;
    private Button botonEntrar;
    private TextView botonIrRegistro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Nivel 4: SharedPreferences (La Libreta)
        // Comprobar si ya hay alguien logueado
        SharedPreferences preferencias = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        boolean estaLogueado = preferencias.getBoolean("sesion_iniciada", false);

        if (estaLogueado) {
            // Si ya está logueado, vamos directo al Home
            irAHome();
            return; // Importante para no cargar el layout de Login
        }

        setContentView(R.layout.actividad_login); // Layout renombrado
        
        // Vincular Vistas (Nivel 1: findViewById)
        campoCorreo = findViewById(R.id.etCorreo);
        campoContrasena = findViewById(R.id.etContrasena);
        botonEntrar = findViewById(R.id.btnEntrar);
        botonIrRegistro = findViewById(R.id.btnIrRegistro);

        // Eventos (Nivel 1: setOnClickListener)
        botonEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hacerLogin();
            }
        });

        botonIrRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Nivel 2: Intents (El Vehículo)
                Intent intentoRegistro = new Intent(ActividadLogin.this, ActividadRegistro.class);
                startActivity(intentoRegistro);
            }
        });
    }

    private void hacerLogin() {
        // Manejo de Texto (Nivel 1: getText().toString())
        String correo = campoCorreo.getText().toString();
        String contrasena = campoContrasena.getText().toString();

        // Validación simple
        if (correo.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Simulación de login exitoso (Aquí iría la comprobación con BD)
        // Nivel 4: Usamos Seguridad para hashear (solo como ejemplo por ahora)
        String hash = Seguridad.encriptarPassword(contrasena);
        
        // Guardar en la "Libreta" que hemos entrado
        SharedPreferences preferencias = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencias.edit();
        editor.putBoolean("sesion_iniciada", true);
        editor.putString("email_usuario", correo);
        editor.apply();

        Toast.makeText(this, "Bienvenido " + correo, Toast.LENGTH_SHORT).show();

        // Nivel 2: Navegación a Home
        irAHome();
    }

    private void irAHome() {
        Intent intentoHome = new Intent(ActividadLogin.this, ActividadPrincipal.class);
        startActivity(intentoHome);
        finish(); // Cerramos login para que no pueda volver atrás
    }
}
