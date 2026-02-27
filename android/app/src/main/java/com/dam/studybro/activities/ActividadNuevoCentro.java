package com.dam.studybro.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dam.studybro.R;
import com.dam.studybro.database.BaseDatosApp;
import com.dam.studybro.database.Centro;
import com.dam.studybro.database.Especialidad;
import com.dam.studybro.database.CentroEspecialidad;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import android.widget.CheckBox;

/**
 * Formulario para uso exclusivo del Administrador que permite
 * agregar centros adicionales manualmente a la base de datos local (Room).
 */
public class ActividadNuevoCentro extends AppCompatActivity {

    private TextInputEditText etNombre, etDireccion, etCiudad, etDescripcion, etWeb;
    private CheckBox cbInfantil, cbPrimaria, cbESO, cbBach, cbSMR, cbDAM, cbDAW, cbASIR;
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
        etWeb         = findViewById(R.id.etWebCentro);
        etDescripcion = findViewById(R.id.etDescripcionCentro);
        
        cbInfantil   = findViewById(R.id.cbInfantil);
        cbPrimaria   = findViewById(R.id.cbPrimaria);
        cbESO        = findViewById(R.id.cbESO);
        cbBach       = findViewById(R.id.cbBachillerato);
        cbSMR        = findViewById(R.id.cbSMR);
        cbDAM        = findViewById(R.id.cbDAM);
        cbDAW        = findViewById(R.id.cbDAW);
        cbASIR       = findViewById(R.id.cbASIR);
        
        btnGuardar    = findViewById(R.id.btnGuardarCentro);

        btnGuardar.setOnClickListener(v -> guardarNuevoCentro());
    }

    private void guardarNuevoCentro() {
        String nombre = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
        String direc  = etDireccion.getText() != null ? etDireccion.getText().toString().trim() : "";
        String ciudad = etCiudad.getText() != null ? etCiudad.getText().toString().trim() : "";
        String web    = etWeb.getText() != null ? etWeb.getText().toString().trim() : "";
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
        c.webUrl = web.isEmpty() ? null : web;
        c.descripcion = desc;
        // Inventamos un código único para evitar conflictos con los 'originarios' de la API de la CAM.
        c.codigoApi = "MANUAL_" + System.currentTimeMillis(); 
        c.valoracionMedia = 0f;

        Executors.newSingleThreadExecutor().execute(() -> {
            BaseDatosApp db = BaseDatosApp.getInstance(getApplicationContext());
            // Insertar centro devuelve el ID generado
            long idCentroInsertado = db.centroDao().insertar(c);
            
            // Buscar IDs de especialidades para vincular
            List<Especialidad> todasLasEspecialidades = db.especialidadDao().obtenerTodas();
            List<CentroEspecialidad> vinculos = new ArrayList<>();
            
            for (Especialidad e : todasLasEspecialidades) {
                if (e.codigoApi.equals("INF") && cbInfantil.isChecked()) vinculos.add(new CentroEspecialidad((int)idCentroInsertado, e.id));
                if (e.codigoApi.equals("PRI") && cbPrimaria.isChecked()) vinculos.add(new CentroEspecialidad((int)idCentroInsertado, e.id));
                if (e.codigoApi.equals("ESO") && cbESO.isChecked()) vinculos.add(new CentroEspecialidad((int)idCentroInsertado, e.id));
                if (e.codigoApi.equals("BACH") && cbBach.isChecked()) vinculos.add(new CentroEspecialidad((int)idCentroInsertado, e.id));
                if (e.codigoApi.equals("SMR") && cbSMR.isChecked()) vinculos.add(new CentroEspecialidad((int)idCentroInsertado, e.id));
                if (e.codigoApi.equals("DAM") && cbDAM.isChecked()) vinculos.add(new CentroEspecialidad((int)idCentroInsertado, e.id));
                if (e.codigoApi.equals("DAW") && cbDAW.isChecked()) vinculos.add(new CentroEspecialidad((int)idCentroInsertado, e.id));
                if (e.codigoApi.equals("ASIR") && cbASIR.isChecked()) vinculos.add(new CentroEspecialidad((int)idCentroInsertado, e.id));
            }
            
            if (!vinculos.isEmpty()) {
                db.centroEspecialidadDao().insertarLista(vinculos);
            }

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
