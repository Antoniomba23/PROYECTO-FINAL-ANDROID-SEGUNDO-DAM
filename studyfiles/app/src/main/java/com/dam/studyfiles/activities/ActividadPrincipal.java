package com.dam.studyfiles.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dam.studyfiles.R;
import com.dam.studyfiles.adapters.AdaptadorArchivos;
import com.dam.studyfiles.adapters.AdaptadorCategorias;
import com.dam.studyfiles.models.Archivo;
import com.dam.studyfiles.network.SupabaseClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActividadPrincipal extends AppCompatActivity {

    private RecyclerView      rvCategorias, rvBusqueda;
    private EditText          etBuscar;
    private AdaptadorArchivos adaptadorBusqueda;
    private List<Archivo>     resultadosBusqueda = new ArrayList<>();
    private boolean           buscando = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_principal);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("StudyFiles");

        etBuscar   = findViewById(R.id.etBuscar);
        rvCategorias = findViewById(R.id.rvCategorias);
        rvBusqueda   = findViewById(R.id.rvBusqueda);

        configurarCategorias();
        configurarBusqueda();

        FloatingActionButton fabSubir = findViewById(R.id.fabSubir);
        fabSubir.setOnClickListener(v ->
                startActivity(new Intent(this, ActividadSubir.class)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Mis Favoritos")
                .setIcon(R.drawable.ic_favorite)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            startActivity(new Intent(this, ActividadFavoritos.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void configurarCategorias() {
        List<AdaptadorCategorias.Categoria> categorias = Arrays.asList(
                new AdaptadorCategorias.Categoria("Historia",         R.drawable.ic_historia,      R.color.cat_historia),
                new AdaptadorCategorias.Categoria("Matemáticas",      R.drawable.ic_matematicas,   R.color.cat_matematicas),
                new AdaptadorCategorias.Categoria("Lengua",           R.drawable.ic_lengua,        R.color.cat_lengua),
                new AdaptadorCategorias.Categoria("Ciencias",         R.drawable.ic_ciencias,      R.color.cat_ciencias),
                new AdaptadorCategorias.Categoria("Informática",      R.drawable.ic_informatica,   R.color.cat_informatica),
                new AdaptadorCategorias.Categoria("Inglés",           R.drawable.ic_ingles,        R.color.cat_ingles),
                new AdaptadorCategorias.Categoria("Arte y Música",    R.drawable.ic_arte,          R.color.cat_arte),
                new AdaptadorCategorias.Categoria("Otros",            R.drawable.ic_otros,         R.color.cat_otros)
        );

        rvCategorias.setLayoutManager(new GridLayoutManager(this, 2));
        rvCategorias.setAdapter(new AdaptadorCategorias(categorias, (nombre, iconRes) -> {
            Intent i = new Intent(this, ActividadArchivos.class);
            i.putExtra("categoria", nombre);
            startActivity(i);
        }));
    }

    private void configurarBusqueda() {
        adaptadorBusqueda = new AdaptadorArchivos(resultadosBusqueda, archivo -> {
            Intent i = new Intent(this, ActividadDetalle.class);
            i.putExtra("archivo_id",    archivo.id);
            i.putExtra("nombre",        archivo.nombre);
            i.putExtra("descripcion",   archivo.descripcion);
            i.putExtra("categoria",     archivo.categoria);
            i.putExtra("uploader",      archivo.uploader);
            i.putExtra("url_archivo",   archivo.urlArchivo);
            i.putExtra("tipo_archivo",  archivo.tipoArchivo);
            i.putExtra("likes",         archivo.likes);
            i.putExtra("dislikes",      archivo.dislikes);
            i.putExtra("reportes",      archivo.reportes);
            startActivity(i);
        });
        rvBusqueda.setLayoutManager(new LinearLayoutManager(this));
        rvBusqueda.setAdapter(adaptadorBusqueda);
        rvBusqueda.setVisibility(android.view.View.GONE);

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.length() >= 2) {
                    buscar(query);
                } else {
                    rvBusqueda.setVisibility(android.view.View.GONE);
                    rvCategorias.setVisibility(android.view.View.VISIBLE);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void buscar(String query) {
        SupabaseClient.getApi()
                .buscarArchivos("ilike.*" + query + "*")
                .enqueue(new Callback<List<Archivo>>() {
                    @Override
                    public void onResponse(Call<List<Archivo>> call, Response<List<Archivo>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            resultadosBusqueda.clear();
                            resultadosBusqueda.addAll(response.body());
                            adaptadorBusqueda.actualizar(resultadosBusqueda);
                            rvBusqueda.setVisibility(android.view.View.VISIBLE);
                            rvCategorias.setVisibility(android.view.View.GONE);
                        }
                    }
                    @Override
                    public void onFailure(Call<List<Archivo>> call, Throwable t) {
                        Toast.makeText(ActividadPrincipal.this,
                                "Error de conexión", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
