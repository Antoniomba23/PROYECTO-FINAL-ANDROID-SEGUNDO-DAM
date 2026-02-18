package com.dam.studybro.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.dam.studybro.R;

public class ActividadPerfilCentro extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_perfil_centro);

        int centroId = getIntent().getIntExtra("centro_id", -1);
        if (centroId == -1) {
            Toast.makeText(this, "Error al cargar centro", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Aquí se cargaran los detalles del centro desde la BD en el futuro
        // Por ahora solo mostramos el layout
    }
}
