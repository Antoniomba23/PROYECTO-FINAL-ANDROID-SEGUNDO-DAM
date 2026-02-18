package com.dam.studybro.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.dam.studybro.R;

public class ActividadNuevaPublicacion extends AppCompatActivity {
    // UI
    private com.google.android.material.textfield.TextInputEditText etTitulo, etDescripcion;
    private android.widget.AutoCompleteTextView spinnerTipo, spinnerAsignatura;
    private android.widget.Button btnPublicar;

    // Data
    private com.dam.studybro.database.BaseDatosApp db;
    private java.util.concurrent.ExecutorService executorService;
    private java.util.List<com.dam.studybro.database.Asignatura> listaAsignaturas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_nueva_publicacion);

        // 1. Init DB
        db = androidx.room.Room.databaseBuilder(getApplicationContext(),
                com.dam.studybro.database.BaseDatosApp.class, "studybro-db").build();
        executorService = java.util.concurrent.Executors.newSingleThreadExecutor();

        // 2. Bind Views
        etTitulo = findViewById(R.id.etTitle);
        etDescripcion = findViewById(R.id.etDescription);
        spinnerTipo = findViewById(R.id.spinnerType);
        spinnerAsignatura = findViewById(R.id.spinnerSubject);
        btnPublicar = findViewById(R.id.btnPublish);

        // 3. Configurar Spinners
        configurarSpinners();

        // 4. Botón Publicar
        btnPublicar.setOnClickListener(v -> publicar());
    }

    private void configurarSpinners() {
        // Tipo: Hardcoded
        String[] tipos = {"APUNTE", "EXAMEN", "DUDA", "TAREA"};
        android.widget.ArrayAdapter<String> adapterTipo = new android.widget.ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, tipos);
        spinnerTipo.setAdapter(adapterTipo);

        // Asignaturas: Desde BD
        executorService.execute(() -> {
            listaAsignaturas = db.asignaturaDao().obtenerTodas();
            
            // Crear lista de nombres para el adapter
            java.util.List<String> nombres = new java.util.ArrayList<>();
            for (com.dam.studybro.database.Asignatura a : listaAsignaturas) {
                nombres.add(a.nombre);
            }

            runOnUiThread(() -> {
                android.widget.ArrayAdapter<String> adapterAsig = new android.widget.ArrayAdapter<>(
                        this, android.R.layout.simple_dropdown_item_1line, nombres);
                spinnerAsignatura.setAdapter(adapterAsig);
            });
        });
    }

    private void publicar() {
        String titulo = etTitulo.getText().toString();
        String desc = etDescripcion.getText().toString();
        String tipo = spinnerTipo.getText().toString();
        String nombreAsignatura = spinnerAsignatura.getText().toString();

        if (titulo.isEmpty() || desc.isEmpty() || tipo.isEmpty() || nombreAsignatura.isEmpty()) {
            Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            // Buscar ID de asignatura
            int asignaturaId = -1;
            for (com.dam.studybro.database.Asignatura a : listaAsignaturas) {
                if (a.nombre.equals(nombreAsignatura)) {
                    asignaturaId = a.id;
                    break;
                }
            }

            // Calcular Año Escolar (Histórico)
            String anioEscolar = calcularAnioEscolar();

            // Guardar Publicacion
            // Usuario ID 1 simulado (debería venir de SharedPreferences)
            com.dam.studybro.database.Publicacion nuevaPub = new com.dam.studybro.database.Publicacion();
            nuevaPub.titulo = titulo;
            nuevaPub.descripcion = desc;
            nuevaPub.tipo = tipo;
            nuevaPub.asignaturaId = asignaturaId;
            nuevaPub.usuarioId = 1; // Simulado
            nuevaPub.fechaSubida = System.currentTimeMillis();
            nuevaPub.anioEscolar = anioEscolar; // "2025-2026"

            db.publicacionDao().insertar(nuevaPub);

            runOnUiThread(() -> {
                Toast.makeText(this, "Publicado en curso: " + anioEscolar, Toast.LENGTH_LONG).show();
                finish();
            });
        });
    }

    private String calcularAnioEscolar() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int mes = cal.get(java.util.Calendar.MONTH); // 0 = Enero, 8 = Septiembre
        int anio = cal.get(java.util.Calendar.YEAR);

        // Si estamos en Septiembre (8) o después, el curso es "Año - Año+1"
        // Si estamos antes, es "Año-1 - Año"
        if (mes >= java.util.Calendar.SEPTEMBER) {
            return anio + "-" + (anio + 1);
        } else {
            return (anio - 1) + "-" + anio;
        }
    }
}
