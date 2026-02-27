package com.dam.studybro.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dam.studybro.R;
import com.dam.studybro.database.BaseDatosApp;
import com.dam.studybro.database.SugerenciaEspecialidad;
import com.dam.studybro.database.SugerenciaMateria;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.Executors;

/**
 * Pantalla donde un estudiante ("ESTUDIANTE") puede sugerir
 * una nueva especialidad o materia que falte en su centro_id actual.
 */
public class ActividadSugerirEntidad extends AppCompatActivity {

    private RadioGroup rgTipoSugerencia;
    private RadioButton rbEspecialidad, rbMateria;
    private TextInputEditText etNombreSugerido, etCursoSugerido;
    private TextInputLayout tilCurso;
    private Button btnEnviar;

    private int centroIdActual = -1;
    private String emailUsuario = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_sugerir);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Buzón de Sugerencias");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Obtener datos del usuario logueado
        SharedPreferences prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        centroIdActual = prefs.getInt("centro_id", -1);
        emailUsuario = prefs.getString("email_usuario", "anonimo@correo.com");

        if (centroIdActual == -1) {
            Toast.makeText(this, "Error: No estás vinculado a un Centro.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        rgTipoSugerencia = findViewById(R.id.rgTipoSugerencia);
        rbEspecialidad = findViewById(R.id.rbEspecialidad);
        rbMateria = findViewById(R.id.rbMateria);
        etNombreSugerido = findViewById(R.id.etNombreSugerido);
        etCursoSugerido = findViewById(R.id.etCursoSugerido);
        tilCurso = findViewById(R.id.tilCurso);
        btnEnviar = findViewById(R.id.btnEnviarSugerencia);

        // Ocultar campo Curso por defecto porque está marcado Especialidad
        tilCurso.setVisibility(View.GONE);

        rgTipoSugerencia.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbMateria) {
                tilCurso.setVisibility(View.VISIBLE);
            } else {
                tilCurso.setVisibility(View.GONE);
            }
        });

        btnEnviar.setOnClickListener(v -> procesarSugerencia());
    }

    private void procesarSugerencia() {
        String nombre = etNombreSugerido.getText() != null ? etNombreSugerido.getText().toString().trim() : "";
        if (nombre.isEmpty()) {
            Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        btnEnviar.setEnabled(false);

        if (rbEspecialidad.isChecked()) {
            // Guardar Sugerencia de Especialidad
            SugerenciaEspecialidad sugerencia = new SugerenciaEspecialidad(nombre, centroIdActual, emailUsuario);
            Executors.newSingleThreadExecutor().execute(() -> {
                BaseDatosApp.getInstance(getApplicationContext()).sugerenciaEspecialidadDao().insertar(sugerencia);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Sugerencia enviada a los administradores 😊", Toast.LENGTH_LONG).show();
                    finish();
                });
            });

        } else if (rbMateria.isChecked()) {
            // Guardar Sugerencia de Materia
            String strCurso = etCursoSugerido.getText() != null ? etCursoSugerido.getText().toString().trim() : "";
            int curso = 1; // Valor por defecto
            if (!strCurso.isEmpty()) {
                try { curso = Integer.parseInt(strCurso); } catch (NumberFormatException ignored) {}
            }
            
            SugerenciaMateria sugerencia = new SugerenciaMateria(nombre, curso, centroIdActual, emailUsuario);
            Executors.newSingleThreadExecutor().execute(() -> {
                BaseDatosApp.getInstance(getApplicationContext()).sugerenciaMateriaDao().insertar(sugerencia);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Sugerencia enviada a los administradores 😊", Toast.LENGTH_LONG).show();
                    finish();
                });
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
