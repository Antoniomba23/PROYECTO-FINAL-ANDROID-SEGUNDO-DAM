package com.dam.studyfiles.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.dam.studyfiles.R;
import com.dam.studyfiles.database.BaseDatos;
import com.dam.studyfiles.database.Favorito;
import com.dam.studyfiles.database.VotoLocal;
import com.dam.studyfiles.network.SupabaseClient;
import com.google.android.material.chip.Chip;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActividadDetalle extends AppCompatActivity {

    private int    archivoId;
    private int    likes, dislikes, reportes;
    private String nombre, descripcion, categoria, uploader, urlArchivo, tipoArchivo;
    private boolean esFavorito = false;

    private TextView tvNombre, tvDesc, tvAutor, tvCategoria, tvVotos;
    private Button   btnLike, btnDislike, btnAbrir, btnFavorito, btnReportar;
    private BaseDatos db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_detalle);

        // Leer datos del Intent
        archivoId   = getIntent().getIntExtra("archivo_id", -1);
        nombre      = getIntent().getStringExtra("nombre");
        descripcion = getIntent().getStringExtra("descripcion");
        categoria   = getIntent().getStringExtra("categoria");
        uploader    = getIntent().getStringExtra("uploader");
        urlArchivo  = getIntent().getStringExtra("url_archivo");
        tipoArchivo = getIntent().getStringExtra("tipo_archivo");
        likes       = getIntent().getIntExtra("likes", 0);
        dislikes    = getIntent().getIntExtra("dislikes", 0);
        reportes    = getIntent().getIntExtra("reportes", 0);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(nombre);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = BaseDatos.getInstance(getApplicationContext());

        // Vincular vistas
        tvNombre    = findViewById(R.id.tvDetalleNombre);
        tvDesc      = findViewById(R.id.tvDetalleDesc);
        tvAutor     = findViewById(R.id.tvDetalleAutor);
        tvCategoria = findViewById(R.id.tvDetalleCategoria);
        tvVotos     = findViewById(R.id.tvDetalleVotos);
        btnLike     = findViewById(R.id.btnLike);
        btnDislike  = findViewById(R.id.btnDislike);
        btnAbrir    = findViewById(R.id.btnAbrirArchivo);
        btnFavorito = findViewById(R.id.btnFavorito);
        btnReportar = findViewById(R.id.btnReportar);

        mostrarDatos();
        comprobarEstadoLocal();

        btnLike.setOnClickListener(v -> votar("like"));
        btnDislike.setOnClickListener(v -> votar("dislike"));
        btnAbrir.setOnClickListener(v -> abrirArchivo());
        btnFavorito.setOnClickListener(v -> toggleFavorito());
        btnReportar.setOnClickListener(v -> confirmarReporte());
    }

    private void mostrarDatos() {
        tvNombre.setText(nombre);
        tvDesc.setText(descripcion != null && !descripcion.isEmpty() ? descripcion : "Sin descripción");
        tvAutor.setText("Subido por: " + (uploader != null ? uploader : "Anónimo"));
        tvCategoria.setText("Categoría: " + (categoria != null ? categoria : "—"));
        actualizarVotos();
    }

    private void actualizarVotos() {
        tvVotos.setText("👍 " + likes + "   👎 " + dislikes);
    }

    /** Comprueba en Room si ya votó y si es favorito */
    private void comprobarEstadoLocal() {
        Executors.newSingleThreadExecutor().execute(() -> {
            String voto      = db.votoLocalDao().obtenerVoto(archivoId);
            boolean fav      = db.favoritoDao().esFavorito(archivoId) > 0;
            runOnUiThread(() -> {
                esFavorito = fav;
                actualizarBotonFavorito();
                if (voto != null) {
                    btnLike.setEnabled(false);
                    btnDislike.setEnabled(false);
                    btnLike.setText("like".equals(voto) ? "✅ Me gusta" : "👍 Me gusta");
                    btnDislike.setText("dislike".equals(voto) ? "✅ No me gusta" : "👎 No me gusta");
                }
            });
        });
    }

    private void votar(String tipo) {
        // Bloquear botones inmediatamente
        btnLike.setEnabled(false);
        btnDislike.setEnabled(false);

        if ("like".equals(tipo)) likes++;
        else dislikes++;

        actualizarVotos();

        // Guardar voto localmente
        Executors.newSingleThreadExecutor().execute(() ->
                db.votoLocalDao().insertar(new VotoLocal(archivoId, tipo)));

        // Actualizar en Supabase
        Map<String, Object> campos = new HashMap<>();
        campos.put("likes",    likes);
        campos.put("dislikes", dislikes);

        SupabaseClient.getApi().actualizarArchivo("eq." + archivoId, campos)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> c, Response<Void> r) {
                        // Si dislikes >= 10, eliminar automáticamente
                        if (dislikes >= 10) eliminarPorMalosDislikes();
                    }
                    @Override
                    public void onFailure(Call<Void> c, Throwable t) {
                        Toast.makeText(ActividadDetalle.this,
                                "Error al guardar voto", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void eliminarPorMalosDislikes() {
        SupabaseClient.getApi().eliminarArchivo("eq." + archivoId)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> c, Response<Void> r) {
                        Toast.makeText(ActividadDetalle.this,
                                "Archivo eliminado por exceso de 'No me gusta'",
                                Toast.LENGTH_LONG).show();
                        finish();
                    }
                    @Override
                    public void onFailure(Call<Void> c, Throwable t) {}
                });
    }

    private void abrirArchivo() {
        if (urlArchivo == null || urlArchivo.isEmpty()) {
            Toast.makeText(this, "URL no disponible", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlArchivo));
        startActivity(browserIntent);
    }

    private void toggleFavorito() {
        Executors.newSingleThreadExecutor().execute(() -> {
            if (esFavorito) {
                db.favoritoDao().eliminarPorId(archivoId);
                esFavorito = false;
            } else {
                db.favoritoDao().insertar(new Favorito(
                        archivoId, nombre, descripcion, categoria,
                        uploader, urlArchivo, tipoArchivo, likes, dislikes));
                esFavorito = true;
            }
            runOnUiThread(() -> {
                actualizarBotonFavorito();
                Toast.makeText(this,
                        esFavorito ? "Añadido a favoritos" : "Eliminado de favoritos",
                        Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void actualizarBotonFavorito() {
        btnFavorito.setText(esFavorito ? "★ Quitar favorito" : "☆ Añadir favorito");
    }

    private void confirmarReporte() {
        new AlertDialog.Builder(this)
                .setTitle("Reportar archivo")
                .setMessage("¿Quieres reportar este archivo como inapropiado?")
                .setPositiveButton("Reportar", (d, w) -> enviarReporte())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void enviarReporte() {
        reportes++;
        Map<String, Object> campos = new HashMap<>();
        campos.put("reportes", reportes);
        SupabaseClient.getApi().actualizarArchivo("eq." + archivoId, campos)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> c, Response<Void> r) {
                        Toast.makeText(ActividadDetalle.this,
                                "Archivo reportado. Gracias.", Toast.LENGTH_SHORT).show();
                        btnReportar.setEnabled(false);
                    }
                    @Override
                    public void onFailure(Call<Void> c, Throwable t) {
                        Toast.makeText(ActividadDetalle.this,
                                "Error al reportar", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
