package com.dam.studyfiles.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.dam.studyfiles.R;
import com.dam.studyfiles.adapters.AdaptadorArchivos;
import com.dam.studyfiles.models.Archivo;
import com.dam.studyfiles.network.SupabaseClient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActividadArchivos extends AppCompatActivity {

    private RecyclerView       rv;
    private ProgressBar        pb;
    private TextView           tvVacio;
    private SwipeRefreshLayout swipe;
    private Spinner            spOrden;
    private AdaptadorArchivos  adaptador;
    private String             categoria;
    private List<Archivo>      listaCompleta = new ArrayList<>();

    private static final String[] ORDENES = {"Más recientes", "Más valorados", "Título A→Z"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_archivos);

        categoria = getIntent().getStringExtra("categoria");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(categoria != null ? categoria : "Archivos");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        rv      = findViewById(R.id.rvArchivos);
        pb      = findViewById(R.id.pbCargando);
        tvVacio = findViewById(R.id.tvVacio);
        swipe   = findViewById(R.id.swipeRefresh);
        spOrden = findViewById(R.id.spOrden);

        adaptador = new AdaptadorArchivos(new ArrayList<>(), this::abrirDetalle);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adaptador);

        // Configurar Spinner de ordenación
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, ORDENES);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spOrden.setAdapter(spinnerAdapter);
        spOrden.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                aplicarOrden(pos);
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        swipe.setOnRefreshListener(this::cargarArchivos);
        cargarArchivos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refrescar la lista al volver (por si se eliminó un archivo)
        cargarArchivos();
    }

    private void aplicarOrden(int indice) {
        if (listaCompleta.isEmpty()) return;
        List<Archivo> ordenada = new ArrayList<>(listaCompleta);
        switch (indice) {
            case 1: // Más valorados
                ordenada.sort((a, b) -> Integer.compare(b.likes - b.dislikes, a.likes - a.dislikes));
                break;
            case 2: // A→Z
                ordenada.sort(Comparator.comparing(a -> a.nombre != null ? a.nombre : ""));
                break;
            default: // Más recientes (ya ordenados por fecha del servidor)
                break;
        }
        adaptador.actualizar(ordenada);
    }

    private void cargarArchivos() {
        pb.setVisibility(View.VISIBLE);
        tvVacio.setVisibility(View.GONE);

        Call<List<Archivo>> call = (categoria != null)
                ? SupabaseClient.getApi().getArchivosPorCategoria("eq." + categoria)
                : SupabaseClient.getApi().getArchivos();

        call.enqueue(new Callback<List<Archivo>>() {
            @Override
            public void onResponse(Call<List<Archivo>> c, Response<List<Archivo>> r) {
                pb.setVisibility(View.GONE);
                swipe.setRefreshing(false);
                if (r.isSuccessful() && r.body() != null) {
                    listaCompleta.clear();
                    listaCompleta.addAll(r.body());
                    aplicarOrden(spOrden.getSelectedItemPosition());
                    tvVacio.setVisibility(listaCompleta.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    Toast.makeText(ActividadArchivos.this,
                            "Error al cargar: " + r.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<Archivo>> c, Throwable t) {
                pb.setVisibility(View.GONE);
                swipe.setRefreshing(false);
                Toast.makeText(ActividadArchivos.this,
                        "Sin conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void abrirDetalle(Archivo a) {
        Intent i = new Intent(this, ActividadDetalle.class);
        i.putExtra("archivo_id",   a.id);
        i.putExtra("nombre",       a.nombre);
        i.putExtra("descripcion",  a.descripcion);
        i.putExtra("categoria",    a.categoria);
        i.putExtra("uploader",     a.uploader);
        i.putExtra("url_archivo",  a.urlArchivo);
        i.putExtra("tipo_archivo", a.tipoArchivo);
        i.putExtra("likes",        a.likes);
        i.putExtra("dislikes",     a.dislikes);
        i.putExtra("reportes",     a.reportes);
        i.putExtra("institucion",  a.institucion);
        i.putExtra("nivel_estudios", a.nivelEstudios);
        i.putExtra("usuario_id",   a.usuarioId);
        startActivity(i);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
