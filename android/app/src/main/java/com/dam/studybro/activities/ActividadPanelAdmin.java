package com.dam.studybro.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.dam.studybro.R;

/**
 * Panel central exclusivo para administradores, desde donde se realizan tareas 
 * de moderación globales sin las restricciones de usuario normal.
 */
public class ActividadPanelAdmin extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_panel_admin);

        Button btnGestionarPublicaciones = findViewById(R.id.btnGestionarPublicaciones);

        // Envía al administrador a la vista completa de publicaciones saltándose los IDs
        Button btnGestionarPubs = findViewById(R.id.btnGestionarPublicaciones);
        Button btnNuevoCentro   = findViewById(R.id.btnNuevoCentro);
        Button btnGestionarUsrs = findViewById(R.id.btnGestionarUsuarios);

        btnGestionarPubs.setOnClickListener(v -> {
            Intent intent = new Intent(this, ActividadPublicaciones.class);
            // Flag especial para que la actividad de publicaciones muestre TODO y permita borrar TODO
            intent.putExtra("modo_admin_global", true);
            startActivity(intent);
        });

        btnNuevoCentro.setOnClickListener(v -> {
            startActivity(new Intent(this, ActividadNuevoCentro.class));
        });

        btnGestionarUsrs.setOnClickListener(v -> {
            startActivity(new Intent(this, ActividadGestionUsuarios.class));
        });
    }
}
