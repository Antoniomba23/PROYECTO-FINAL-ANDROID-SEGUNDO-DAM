package com.dam.studybro.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.dam.studybro.R;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Activity de Registro - Pantalla de creación de cuenta
 * Responsable: Antonio
 */
public class ActividadRegistro extends AppCompatActivity {
    
    // Variables
    private TextInputEditText campoNombre;
    private TextInputEditText campoCorreoRegistro;
    private TextInputEditText campoContrasenaRegistro;
    private Button botonRegistrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_registro); // Layout renombrado
        
        // Vincular Vistas
        campoNombre = findViewById(R.id.etNombreUsuario);
        campoCorreoRegistro = findViewById(R.id.etCorreoRegistro);
        campoContrasenaRegistro = findViewById(R.id.etContrasenaRegistro);
        botonRegistrar = findViewById(R.id.btnRegistrar);

        // Eventos
        botonRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrarUsuario();
            }
        });
    }

    private void registrarUsuario() {
        String nombre = campoNombre.getText().toString();
        String correo = campoCorreoRegistro.getText().toString();
        String contrasena = campoContrasenaRegistro.getText().toString();

        if (nombre.isEmpty() || correo.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Aquí iría la lógica de guardar en Base de Datos (Nivel 4)
        Toast.makeText(this, "Usuario registrado: " + nombre, Toast.LENGTH_SHORT).show();
        
        // Volver atrás (o ir al login/home)
        finish();
    }
}
