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
    
    // DB y Executor
    private com.dam.studybro.database.BaseDatosApp db;
    private java.util.concurrent.ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar DB y Executor
        // Inicializar DB y Executor
        db = com.dam.studybro.database.BaseDatosApp.getInstance(getApplicationContext());
        executorService = java.util.concurrent.Executors.newSingleThreadExecutor();

        // Nivel 4: SharedPreferences (La Libreta)
        // Comprobar si ya hay alguien logueado
        SharedPreferences preferencias = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        boolean estaLogueado = preferencias.getBoolean("sesion_iniciada", false);

        if (estaLogueado) {
            irAHome();
            return;
        }

        setContentView(R.layout.actividad_login);
        
        // Vincular Vistas
        campoCorreo = findViewById(R.id.etCorreo);
        campoContrasena = findViewById(R.id.etContrasena);
        botonEntrar = findViewById(R.id.btnEntrar);
        botonIrRegistro = findViewById(R.id.btnIrRegistro);

        // Eventos
        botonEntrar.setOnClickListener(v -> hacerLogin());

        botonIrRegistro.setOnClickListener(v -> {
            Intent intentoRegistro = new Intent(ActividadLogin.this, ActividadRegistro.class);
            startActivity(intentoRegistro);
        });
    }

    private void hacerLogin() {
        String correo = campoCorreo.getText().toString();
        String contrasena = campoContrasena.getText().toString();

        if (correo.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lógica de Login Real en segundo plano
        executorService.execute(() -> {
            // Buscamos usuario en la BD
            com.dam.studybro.database.Usuario usuario = db.usuarioDao().buscarPorCorreo(correo);

            runOnUiThread(() -> {
                if (usuario != null) {
                    // Verificar contraseña (en una app real se usa hash, aquí simulamos que coinciden si el usuario existe para la demo, o se compara simple)
                    // Para simplificar en nivel principiante, asumimos que si existe el email es correcto para la demo
                    // O comparamos con el campo password si lo tuviéramos en claro (mala práctica) o hash.
                    // Vamos a comparar:
                    if (usuario.passwordHash.equals(Seguridad.encriptarPassword(contrasena))) {
                        // Login Exitoso
                        guardarSesion(usuario);
                    } else {
                        Toast.makeText(ActividadLogin.this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ActividadLogin.this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void guardarSesion(com.dam.studybro.database.Usuario usuario) {
        SharedPreferences preferencias = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencias.edit();
        editor.putBoolean("sesion_iniciada", true);
        editor.putString("email_usuario", usuario.email);
        
        // GUARDAMOS EL ID DEL CENTRO (O -1 SI ES NULL/INVITADO)
        int centroId = usuario.centroId != null ? usuario.centroId : -1;
        editor.putInt("centro_id", centroId);
        
        editor.apply();

        Toast.makeText(this, "Bienvenido " + usuario.nombre, Toast.LENGTH_SHORT).show();
        irAHome();
    }

    private void irAHome() {
        Intent intentoHome = new Intent(ActividadLogin.this, ActividadPrincipal.class);
        startActivity(intentoHome);
        finish();
    }
}
