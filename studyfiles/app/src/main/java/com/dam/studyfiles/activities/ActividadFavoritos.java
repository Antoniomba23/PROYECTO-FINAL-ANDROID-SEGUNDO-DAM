package com.dam.studyfiles.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dam.studyfiles.R;
import com.dam.studyfiles.adapters.AdaptadorFavoritos;
import com.dam.studyfiles.database.BaseDatos;
import com.dam.studyfiles.database.Favorito;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class ActividadFavoritos extends AppCompatActivity {

    private RecyclerView       rv;
    private TextView           tvVacio;
    private AdaptadorFavoritos adaptador;
    private BaseDatos          db;
    private List<Favorito>     lista = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_favoritos);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Mis Favoritos");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db      = BaseDatos.getInstance(getApplicationContext());
        rv      = findViewById(R.id.rvFavoritos);
        tvVacio = findViewById(R.id.tvVacioFavoritos);

        adaptador = new AdaptadorFavoritos(lista, fav -> {
            Intent i = new Intent(this, ActividadDetalle.class);
            i.putExtra("archivo_id",   fav.id);
            i.putExtra("nombre",       fav.nombre);
            i.putExtra("descripcion",  fav.descripcion);
            i.putExtra("categoria",    fav.categoria);
            i.putExtra("uploader",     fav.uploader);
            i.putExtra("url_archivo",  fav.urlArchivo);
            i.putExtra("tipo_archivo", fav.tipoArchivo);
            i.putExtra("likes",        fav.likes);
            i.putExtra("dislikes",     fav.dislikes);
            i.putExtra("reportes",     0);
            startActivity(i);
        });

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adaptador);

        // Swipe para eliminar favorito
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView rv, RecyclerView.ViewHolder a,
                                  RecyclerView.ViewHolder b) { return false; }
            @Override
            public void onSwiped(RecyclerView.ViewHolder vh, int dir) {
                Favorito fav = adaptador.getItem(vh.getAdapterPosition());
                Executors.newSingleThreadExecutor().execute(() -> {
                    db.favoritoDao().eliminarPorId(fav.id);
                    runOnUiThread(() -> {
                        lista.remove(fav);
                        adaptador.notifyDataSetChanged();
                        tvVacio.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
                        Toast.makeText(ActividadFavoritos.this,
                                "Favorito eliminado", Toast.LENGTH_SHORT).show();
                    });
                });
            }
        }).attachToRecyclerView(rv);

        cargarFavoritos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarFavoritos();
    }

    private void cargarFavoritos() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Favorito> favoritos = db.favoritoDao().obtenerTodos();
            runOnUiThread(() -> {
                lista.clear();
                lista.addAll(favoritos);
                adaptador.notifyDataSetChanged();
                tvVacio.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
