package com.dam.studybro.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dam.studybro.R;
import com.dam.studybro.adapters.AdaptadorCentros;
import com.dam.studybro.database.BaseDatosApp;
import com.dam.studybro.database.Centro;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Pantalla que aparece una sola vez tras el primer login.
 * El usuario selecciona su centro educativo y se guarda en SharedPreferences.
 */
public class ActividadSeleccionarCentro extends AppCompatActivity {

    private AdaptadorCentros adaptador;
    private BaseDatosApp db;
    private ExecutorService executor;
    private List<Centro> listaTodos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_seleccionar_centro);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Mi centro");
            getSupportActionBar().setDisplayHomeAsUpEnabled(false); // No se puede volver atrás
        }

        db       = BaseDatosApp.getInstance(getApplicationContext());
        executor = Executors.newSingleThreadExecutor();

        // RecyclerView con la lista de centros
        RecyclerView rv = findViewById(R.id.rvCentros);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adaptador = new AdaptadorCentros(new ArrayList<>(), centro -> {
            // El usuario ha pulsado un centro → guardarlo
            guardarCentroYContinuar(centro);
        });
        rv.setAdapter(adaptador);

        // Buscador
        com.google.android.material.textfield.TextInputEditText etBuscar =
                findViewById(R.id.etBuscarCentro);
        etBuscar.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                adaptador.filtrar(s.toString());
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        // Botón saltar → ir sin centro asignado
        findViewById(R.id.btnSaltar).setOnClickListener(v -> irAHome());

        // Cargar centros desde Room
        cargarCentros();
    }

    private void cargarCentros() {
        executor.execute(() -> {
            List<Centro> centros = db.centroDao().obtenerTodos();
            listaTodos = centros;
            runOnUiThread(() -> adaptador.actualizarDatos(centros));
        });
    }

    /** Guarda centro_id en SharedPreferences y vuelve */
    private void guardarCentroYContinuar(Centro centro) {
        SharedPreferences prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        prefs.edit()
                .putInt("centro_id", centro.id)
                .putString("nombre_centro", centro.nombre)
                .apply();

        Toast.makeText(this, "Centro: " + centro.nombre, Toast.LENGTH_SHORT).show();

        // Si venimos del login (primer setup) → home; si lo cambiamos desde el menú → volver atrás
        boolean esPrimerSetup = getIntent().getBooleanExtra("primer_setup", false);
        if (esPrimerSetup) {
            irAHome();
        } else {
            finish(); // Vuelve a la pantalla anterior (home con drawer actualizado en onResume)
        }
    }

    private void irAHome() {
        startActivity(new Intent(this, ActividadPrincipal.class));
        finish();
    }
}
