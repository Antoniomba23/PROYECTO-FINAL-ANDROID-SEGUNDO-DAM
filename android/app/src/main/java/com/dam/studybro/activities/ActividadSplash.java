package com.dam.studybro.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.dam.studybro.R;

/**
 * Pantalla de inicio que muestra el logo al abrir la app.
 */
public class ActividadSplash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_splash);

        // Esperar 2 segundos y saltar a la actividad principal
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(ActividadSplash.this, ActividadPrincipal.class);
            startActivity(intent);
            finish(); // Cerrar el splash para que no vuelva atrás al pulsar el botón de retroceso
        }, 2000);
    }
}
