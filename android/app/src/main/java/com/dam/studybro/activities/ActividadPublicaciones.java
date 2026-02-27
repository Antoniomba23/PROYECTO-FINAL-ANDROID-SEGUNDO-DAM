package com.dam.studybro.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dam.studybro.R;
import com.dam.studybro.adapters.AdaptadorPublicaciones;
import com.dam.studybro.database.BaseDatosApp;
import com.dam.studybro.database.Publicacion;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.text.Normalizer;

/**
 * Lista de publicaciones de una asignatura con búsqueda y ordenación.
 * El FAB de nueva publicación sólo es visible si hay sesión activa.
 */
public class ActividadPublicaciones extends AppCompatActivity {

    public static final String EXTRA_ASIGNATURA_ID   = "asignatura_id";
    public static final String EXTRA_ASIGNATURA_NOMBRE = "asignatura_nombre";

    private BaseDatosApp db;
    private ExecutorService executor;
    private AdaptadorPublicaciones adaptador;
    private List<Publicacion> listaCompleta = new ArrayList<>();

    private int asignaturaId;
    private int ordenActual = 0; // 0=más reciente, 1=más antigua, 2=más útiles
    private boolean modoAdminGlobal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_publicaciones);

        db       = BaseDatosApp.getInstance(getApplicationContext());
        executor = Executors.newSingleThreadExecutor();

        modoAdminGlobal = getIntent().getBooleanExtra("modo_admin_global", false);
        // En modo admin, forzamos Id -1 (carga general)
        asignaturaId = modoAdminGlobal ? -1 : getIntent().getIntExtra(EXTRA_ASIGNATURA_ID, -1);
        String nombreAsignatura = modoAdminGlobal ? "Panel Admin: Todas las Pub." : getIntent().getStringExtra(EXTRA_ASIGNATURA_NOMBRE);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(nombreAsignatura != null ? nombreAsignatura : "Publicaciones");
        }

        // RecyclerView
        RecyclerView rv = findViewById(R.id.recyclerPublicaciones);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adaptador = new AdaptadorPublicaciones(new ArrayList<>(), pub -> {
            Intent intent = new Intent(this, ActividadDetalle.class);
            intent.putExtra(ActividadDetalle.EXTRA_PUBLICACION_ID, pub.id);
            startActivity(intent);
        });
        rv.setAdapter(adaptador);

        // Búsqueda en tiempo real
        TextInputEditText etBuscar = findViewById(R.id.etBuscar);
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                filtrarYOrdenar(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Spinner de orden
        Spinner spinner = findViewById(R.id.spinnerOrden);
        ArrayAdapter<String> adpSpinner = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Más recientes", "Más antiguas", "Más útiles"});
        adpSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adpSpinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                ordenActual = pos;
                filtrarYOrdenar(etBuscar.getText() != null ? etBuscar.getText().toString() : "");
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        // FAB: solo para usuarios logueados y NO en modo Admin global
        SharedPreferences prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        boolean sesion = prefs.getBoolean("sesion_iniciada", false);
        FloatingActionButton fab = findViewById(R.id.fabNuevaPublicacion);
        if (sesion && !modoAdminGlobal) {
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(v -> startActivity(
                    new Intent(this, ActividadNuevaPublicacion.class)
                            .putExtra("asignatura_id", asignaturaId)));
        } else {
            fab.setVisibility(View.GONE);
        }

        // Cargar publicaciones
        cargarPublicaciones();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarPublicaciones(); // Refrescar al volver de nueva publicación
    }

    private void cargarPublicaciones() {
        executor.execute(() -> {
            List<Publicacion> lista;
            if (asignaturaId == -1) {
                lista = db.publicacionDao().obtenerTodas();
            } else {
                lista = db.publicacionDao().obtenerPorAsignatura(asignaturaId);
            }
            listaCompleta.clear();
            listaCompleta.addAll(lista);
            runOnUiThread(() -> filtrarYOrdenar(""));
        });
    }

    private void filtrarYOrdenar(String query) {
        List<Publicacion> resultado = new ArrayList<>();
        String q = quitarTildes(query.toLowerCase().trim());
        for (Publicacion p : listaCompleta) {
            String tituloNorm = p.titulo != null ? quitarTildes(p.titulo.toLowerCase()) : "";
            String descNorm = p.descripcion != null ? quitarTildes(p.descripcion.toLowerCase()) : "";
            if (q.isEmpty() || tituloNorm.contains(q) || descNorm.contains(q)) {
                resultado.add(p);
            }
        }
        // Ordenar
        switch (ordenActual) {
            case 0: resultado.sort((a, b) -> Long.compare(b.fechaSubida, a.fechaSubida)); break;
            case 1: resultado.sort(Comparator.comparingLong(a -> a.fechaSubida));         break;
            case 2: // Por likes: requeriría consulta extra; aquí usamos fecha como fallback
                    resultado.sort((a, b) -> Long.compare(b.fechaSubida, a.fechaSubida)); break;
        }
        adaptador.actualizarDatos(resultado);
        TextView tvVacio = findViewById(R.id.tvNoPublicaciones);
        if (tvVacio != null) {
            tvVacio.setVisibility(resultado.isEmpty() ? View.VISIBLE : View.GONE);
            if (resultado.isEmpty()) {
                tvVacio.setText(modoAdminGlobal ? "No hay ninguna publicación registrada en el sistema" : "No hay publicaciones para esta asignatura");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        return super.onOptionsItemSelected(item);
    }

    private String quitarTildes(String texto) {
        if (texto == null) return null;
        String normalizado = Normalizer.normalize(texto, Normalizer.Form.NFD);
        return normalizado.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}
