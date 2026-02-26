package com.dam.studybro.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dam.studybro.R;
import com.dam.studybro.database.BaseDatosApp;
import com.dam.studybro.database.Centro;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.Executors;

/**
 * Formulario para uso exclusivo del Administrador que permite
 * agregar centros adicionales manualmente a la base de datos local (Room).
 */
public class ActividadNuevoCentro extends AppCompatActivity {

    private TextInputEditText etNombre, etDireccion, etCiudad, etDescripcion;
    private Button btnGuardar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_nuevo_centro);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Añadir Centro Manual");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etNombre      = findViewById(R.id.etNombreCentro);
        etDireccion   = findViewById(R.id.etDireccionCentro);
        etCiudad      = findViewById(R.id.etCiudadCentro);
        etDescripcion = findViewById(R.id.etDescripcionCentro);
        btnGuardar    = findViewById(R.id.btnGuardarCentro);

        btnGuardar.setOnClickListener(v -> guardarNuevoCentro());
    }

    private void guardarNuevoCentro() {
        String nombre = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
        String direc  = etDireccion.getText() != null ? etDireccion.getText().toString().trim() : "";
        String ciudad = etCiudad.getText() != null ? etCiudad.getText().toString().trim() : "";
        String desc   = etDescripcion.getText() != null ? etDescripcion.getText().toString().trim() : "";

        if (nombre.isEmpty()) {
            Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }

        btnGuardar.setEnabled(false);
        Centro c = new Centro();
        c.nombre = nombre;
        c.direccion = direc;
        c.ciudad = ciudad;
        c.descripcion = desc;
        // Inventamos un código único para evitar conflictos con los 'originarios' de la API de la CAM.
        c.codigoApi = "MANUAL_" + System.currentTimeMillis(); 
        c.valoracionMedia = 0f;

        Executors.newSingleThreadExecutor().execute(() -> {
            BaseDatosApp db = BaseDatosApp.getInstance(getApplicationContext());
            db.centroDao().insertar(c);

            runOnUiThread(() -> {
                Toast.makeText(ActividadNuevoCentro.this, "Centro añadido correctamente", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
