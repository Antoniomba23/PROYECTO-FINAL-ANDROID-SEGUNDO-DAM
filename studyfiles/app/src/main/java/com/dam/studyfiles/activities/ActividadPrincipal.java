package com.dam.studyfiles.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dam.studyfiles.R;
import com.dam.studyfiles.adapters.AdaptadorArchivos;
import com.dam.studyfiles.adapters.AdaptadorCarpetas;
import com.dam.studyfiles.adapters.AdaptadorCategorias;
import com.dam.studyfiles.adapters.AdaptadorFavoritos;
import com.dam.studyfiles.adapters.AdaptadorMisArchivos;
import com.dam.studyfiles.database.BaseDatos;
import com.dam.studyfiles.database.Favorito;
import com.dam.studyfiles.models.Archivo;
import com.dam.studyfiles.models.MiArchivo;
import com.dam.studyfiles.models.MiCarpeta;
import com.dam.studyfiles.network.SupabaseClient;
import com.dam.studyfiles.utils.DispositivoUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActividadPrincipal extends AppCompatActivity {

    // ── Vistas ────────────────────────────────────────────────────────────────
    private EditText          etBuscar;
    private ProgressBar       pbBusqueda;
    private FloatingActionButton fab;

    // Sección Inicio
    private View              seccionInicio;
    private RecyclerView      rvCategorias, rvBusqueda;
    private AdaptadorArchivos adaptadorBusqueda;

    // Sección Mi Nube
    private View              seccionMiNube;
    private RecyclerView      rvCarpetas, rvArchivosSueltos;
    private TextView          tvVacioNube;
    private AdaptadorCarpetas adaptadorCarpetas;
    private AdaptadorMisArchivos adaptadorArchivosSueltos;

    // Sección Favoritos
    private View              seccionFavoritos;
    private RecyclerView      rvFavoritos;
    private TextView          tvVacioFav;
    private AdaptadorFavoritos adaptadorFavoritos;

    // Datos
    private List<Archivo>    resultadosBusqueda = new ArrayList<>();
    private List<MiCarpeta>  listaCarpetas       = new ArrayList<>();
    private List<MiArchivo>  listaArchivosSueltos = new ArrayList<>();
    private List<Favorito>   listaFavoritos       = new ArrayList<>();
    private String getIdentificador() {
        String userId = DispositivoUtils.getUsuarioId(this);
        return userId != null ? "user_" + userId : DispositivoUtils.getUUID(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Aplicar modo claro/oscuro guardado
        boolean dark = DispositivoUtils.isDarkMode(this);
        AppCompatDelegate.setDefaultNightMode(
                dark ? AppCompatDelegate.MODE_NIGHT_YES
                     : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_principal);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("StudyFiles");

        vincularVistas();
        configurarCategorias();
        configurarBusqueda();
        configurarMiNube();
        configurarFavoritos();
        configurarBottomNav();

        fab.setOnClickListener(v -> onFabClick());
    }

    private void vincularVistas() {
        etBuscar   = findViewById(R.id.etBuscar);
        pbBusqueda = findViewById(R.id.pbBusqueda);
        fab        = findViewById(R.id.fab);

        seccionInicio  = findViewById(R.id.seccionInicio);
        rvCategorias   = findViewById(R.id.rvCategorias);
        rvBusqueda     = findViewById(R.id.rvBusqueda);

        seccionMiNube       = findViewById(R.id.seccionMiNube);
        rvCarpetas          = findViewById(R.id.rvCarpetas);
        rvArchivosSueltos   = findViewById(R.id.rvArchivosSueltos);
        tvVacioNube         = findViewById(R.id.tvVacioNube);

        seccionFavoritos = findViewById(R.id.seccionFavoritos);
        rvFavoritos      = findViewById(R.id.rvFavoritos);
        tvVacioFav       = findViewById(R.id.tvVacioFav);
    }

    // ── CATEGORÍAS / INICIO ───────────────────────────────────────────────────

    private void configurarCategorias() {
        List<AdaptadorCategorias.Categoria> cats = Arrays.asList(
                new AdaptadorCategorias.Categoria("Historia",       R.drawable.ic_historia,    R.color.cat_historia),
                new AdaptadorCategorias.Categoria("Matemáticas",    R.drawable.ic_matematicas, R.color.cat_matematicas),
                new AdaptadorCategorias.Categoria("Lengua",         R.drawable.ic_lengua,      R.color.cat_lengua),
                new AdaptadorCategorias.Categoria("Ciencias",       R.drawable.ic_ciencias,    R.color.cat_ciencias),
                new AdaptadorCategorias.Categoria("Informática",    R.drawable.ic_informatica, R.color.cat_informatica),
                new AdaptadorCategorias.Categoria("Inglés",         R.drawable.ic_ingles,      R.color.cat_ingles),
                new AdaptadorCategorias.Categoria("Arte y Música",  R.drawable.ic_arte,        R.color.cat_arte),
                new AdaptadorCategorias.Categoria("Otros",          R.drawable.ic_otros,       R.color.cat_otros)
        );
        rvCategorias.setLayoutManager(new GridLayoutManager(this, 2));
        rvCategorias.setAdapter(new AdaptadorCategorias(cats, (nombre, icon) -> {
            Intent i = new Intent(this, ActividadArchivos.class);
            i.putExtra("categoria", nombre);
            startActivity(i);
        }));
    }

    private void configurarBusqueda() {
        adaptadorBusqueda = new AdaptadorArchivos(resultadosBusqueda, a -> {
            Intent i = new Intent(this, ActividadDetalle.class);
            i.putExtra("archivo_id",  a.id);     i.putExtra("nombre",       a.nombre);
            i.putExtra("descripcion", a.descripcion); i.putExtra("categoria", a.categoria);
            i.putExtra("uploader",    a.uploader); i.putExtra("url_archivo", a.urlArchivo);
            i.putExtra("tipo_archivo",a.tipoArchivo); i.putExtra("likes",    a.likes);
            i.putExtra("dislikes",    a.dislikes); i.putExtra("reportes",   a.reportes);
            i.putExtra("institucion", a.institucion);
            i.putExtra("nivel_estudios", a.nivelEstudios);
            startActivity(i);
        });
        rvBusqueda.setLayoutManager(new LinearLayoutManager(this));
        rvBusqueda.setAdapter(adaptadorBusqueda);
        rvBusqueda.setVisibility(View.GONE);

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int a,int b,int c){}
            @Override public void afterTextChanged(Editable s){}
            @Override public void onTextChanged(CharSequence s,int a,int b,int c) {
                String q = s.toString().trim();
                if (q.length() >= 2) buscar(q);
                else {
                    rvBusqueda.setVisibility(View.GONE);
                    rvCategorias.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void buscar(String q) {
        pbBusqueda.setVisibility(View.VISIBLE);
        SupabaseClient.getApi().buscarArchivos("ilike.*" + q + "*")
                .enqueue(new Callback<List<Archivo>>() {
                    @Override public void onResponse(Call<List<Archivo>> c, Response<List<Archivo>> r) {
                        pbBusqueda.setVisibility(View.GONE);
                        if (r.isSuccessful() && r.body() != null) {
                            resultadosBusqueda.clear();
                            resultadosBusqueda.addAll(r.body());
                            adaptadorBusqueda.actualizar(resultadosBusqueda);
                            rvBusqueda.setVisibility(View.VISIBLE);
                            rvCategorias.setVisibility(View.GONE);
                        }
                    }
                    @Override public void onFailure(Call<List<Archivo>> c, Throwable t) {
                        pbBusqueda.setVisibility(View.GONE);
                    }
                });
    }

    // ── MI NUBE ───────────────────────────────────────────────────────────────

    private void configurarMiNube() {
        adaptadorCarpetas = new AdaptadorCarpetas(listaCarpetas,
                carpeta -> {
                    Intent i = new Intent(this, ActividadCarpeta.class);
                    i.putExtra("carpeta_id",     carpeta.id);
                    i.putExtra("carpeta_nombre", carpeta.nombre);
                    startActivity(i);
                },
                (carpeta, anchor) -> mostrarMenuCarpeta(carpeta)
        );
        rvCarpetas.setLayoutManager(new GridLayoutManager(this, 2));
        rvCarpetas.setAdapter(adaptadorCarpetas);

        adaptadorArchivosSueltos = new AdaptadorMisArchivos(listaArchivosSueltos,
                a -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(a.urlArchivo))),
                (a, v) -> eliminarMiArchivo(a)
        );
        rvArchivosSueltos.setLayoutManager(new LinearLayoutManager(this));
        rvArchivosSueltos.setAdapter(adaptadorArchivosSueltos);
    }

    private void cargarMiNube() {
        String iden = getIdentificador();
        SupabaseClient.getMiNube().getMisCarpetas("eq." + iden)
                .enqueue(new Callback<List<MiCarpeta>>() {
                    @Override public void onResponse(Call<List<MiCarpeta>> c, Response<List<MiCarpeta>> r) {
                        if (r.isSuccessful() && r.body() != null) {
                            listaCarpetas.clear();
                            listaCarpetas.addAll(r.body());
                            adaptadorCarpetas.actualizar(listaCarpetas);
                        }
                    }
                    @Override public void onFailure(Call<List<MiCarpeta>> c, Throwable t) {
                        Toast.makeText(ActividadPrincipal.this, "Sin conexión", Toast.LENGTH_SHORT).show();
                    }
                });

        SupabaseClient.getMiNube().getMisArchivosSinCarpeta("eq." + iden, "is.null")
                .enqueue(new Callback<List<MiArchivo>>() {
                    @Override public void onResponse(Call<List<MiArchivo>> c, Response<List<MiArchivo>> r) {
                        if (r.isSuccessful() && r.body() != null) {
                            listaArchivosSueltos.clear();
                            listaArchivosSueltos.addAll(r.body());
                            adaptadorArchivosSueltos.actualizar(listaArchivosSueltos);
                            tvVacioNube.setVisibility(
                                    listaCarpetas.isEmpty() && listaArchivosSueltos.isEmpty()
                                    ? View.VISIBLE : View.GONE);
                        }
                    }
                    @Override public void onFailure(Call<List<MiArchivo>> c, Throwable t) {}
                });
    }

    private void mostrarMenuCarpeta(MiCarpeta carpeta) {
        new AlertDialog.Builder(this)
                .setTitle(carpeta.nombre)
                .setItems(new String[]{"✏️ Renombrar", "🗑 Eliminar"}, (d, w) -> {
                    if (w == 0) renombrarCarpeta(carpeta);
                    else eliminarCarpeta(carpeta);
                }).show();
    }

    private void renombrarCarpeta(MiCarpeta carpeta) {
        EditText et = new EditText(this);
        et.setText(carpeta.nombre);
        new AlertDialog.Builder(this)
                .setTitle("Renombrar carpeta")
                .setView(et)
                .setPositiveButton("Guardar", (d, w) -> {
                    String nuevo = et.getText().toString().trim();
                    if (nuevo.isEmpty()) return;
                    Map<String, Object> body = new HashMap<>();
                    body.put("nombre", nuevo);
                    SupabaseClient.getMiNube().renombrarCarpeta("eq." + carpeta.id, body)
                            .enqueue(new Callback<Void>() {
                                @Override public void onResponse(Call<Void> c, Response<Void> r) { cargarMiNube(); }
                                @Override public void onFailure(Call<Void> c, Throwable t) {}
                            });
                })
                .setNegativeButton("Cancelar", null).show();
    }

    private void eliminarCarpeta(MiCarpeta carpeta) {
        new AlertDialog.Builder(this)
                .setMessage("¿Eliminar la carpeta '" + carpeta.nombre + "' y todo su contenido?")
                .setPositiveButton("Eliminar", (d, w) ->
                        SupabaseClient.getMiNube().eliminarCarpeta("eq." + carpeta.id)
                                .enqueue(new Callback<Void>() {
                                    @Override public void onResponse(Call<Void> c, Response<Void> r) { cargarMiNube(); }
                                    @Override public void onFailure(Call<Void> c, Throwable t) {}
                                }))
                .setNegativeButton("Cancelar", null).show();
    }

    private void eliminarMiArchivo(MiArchivo a) {
        SupabaseClient.getMiNube().eliminarMiArchivo("eq." + a.id)
                .enqueue(new Callback<Void>() {
                    @Override public void onResponse(Call<Void> c, Response<Void> r) { cargarMiNube(); }
                    @Override public void onFailure(Call<Void> c, Throwable t) {}
                });
    }

    private void crearCarpeta() {
        String[] colores = {"#1565C0","#2E7D32","#E65100","#6A1B9A","#C62828","#00695C"};
        EditText et = new EditText(this);
        et.setHint("Nombre de la carpeta");
        new AlertDialog.Builder(this)
                .setTitle("Nueva carpeta")
                .setView(et)
                .setPositiveButton("Crear", (d, w) -> {
                    String nombre = et.getText().toString().trim();
                    if (nombre.isEmpty()) return;
                    int idx = (int)(Math.random() * colores.length);
                    Map<String, Object> body = new HashMap<>();
                    body.put("nombre",         nombre);
                    body.put("usuario_id",     getIdentificador());
                    body.put("color",          colores[idx]);
                    body.put("fecha_creacion", System.currentTimeMillis());
                    SupabaseClient.getMiNube().crearCarpeta(body)
                            .enqueue(new Callback<List<MiCarpeta>>() {
                                @Override public void onResponse(Call<List<MiCarpeta>> c, Response<List<MiCarpeta>> r) { cargarMiNube(); }
                                @Override public void onFailure(Call<List<MiCarpeta>> c, Throwable t) {}
                            });
                })
                .setNegativeButton("Cancelar", null).show();
    }

    // ── FAVORITOS ─────────────────────────────────────────────────────────────

    private void configurarFavoritos() {
        adaptadorFavoritos = new AdaptadorFavoritos(listaFavoritos, fav -> {
            Intent i = new Intent(this, ActividadDetalle.class);
            i.putExtra("archivo_id",   fav.id);   i.putExtra("nombre",       fav.nombre);
            i.putExtra("descripcion",  fav.descripcion); i.putExtra("categoria", fav.categoria);
            i.putExtra("uploader",     fav.uploader); i.putExtra("url_archivo", fav.urlArchivo);
            i.putExtra("tipo_archivo", fav.tipoArchivo); i.putExtra("likes",    fav.likes);
            i.putExtra("dislikes",     fav.dislikes); i.putExtra("reportes",   0);
            startActivity(i);
        });
        rvFavoritos.setLayoutManager(new LinearLayoutManager(this));
        rvFavoritos.setAdapter(adaptadorFavoritos);
    }

    private void cargarFavoritos() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Favorito> favs = BaseDatos.getInstance(getApplicationContext())
                    .favoritoDao().obtenerTodos();
            runOnUiThread(() -> {
                listaFavoritos.clear();
                listaFavoritos.addAll(favs);
                adaptadorFavoritos.notifyDataSetChanged();
                tvVacioFav.setVisibility(favs.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }

    // ── BOTTOM NAVIGATION ─────────────────────────────────────────────────────

    private void configurarBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_inicio) {
                mostrarSeccion(seccionInicio);
                etBuscar.setVisibility(View.VISIBLE);
                fab.setVisibility(View.VISIBLE);
                fab.setImageResource(R.drawable.ic_add);
            } else if (id == R.id.nav_mi_nube) {
                mostrarSeccion(seccionMiNube);
                etBuscar.setVisibility(View.GONE);
                fab.setVisibility(View.VISIBLE);
                fab.setImageResource(R.drawable.ic_folder);
                cargarMiNube();
            } else if (id == R.id.nav_favoritos) {
                mostrarSeccion(seccionFavoritos);
                etBuscar.setVisibility(View.GONE);
                fab.setVisibility(View.GONE);
                cargarFavoritos();
            }
            return true;
        });
        // Mostrar Inicio por defecto
        mostrarSeccion(seccionInicio);
    }

    private void mostrarSeccion(View seccion) {
        seccionInicio.setVisibility(View.GONE);
        seccionMiNube.setVisibility(View.GONE);
        seccionFavoritos.setVisibility(View.GONE);
        seccion.setVisibility(View.VISIBLE);
    }

    private void onFabClick() {
        if (seccionMiNube.getVisibility() == View.VISIBLE) {
            new AlertDialog.Builder(this)
                    .setTitle("Mi Nube")
                    .setItems(new String[]{"📁 Nueva carpeta", "☁️ Subir archivo aquí"}, (d, w) -> {
                        if (w == 0) crearCarpeta();
                        else startActivity(new Intent(this, ActividadSubir.class));
                    }).show();
        } else {
            startActivity(new Intent(this, ActividadSubir.class));
        }
    }

    // ── MENÚ TOOLBAR (Modo Oscuro) ────────────────────────────────────────────

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Modo oscuro")
                .setIcon(R.drawable.ic_moon)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        
        if (DispositivoUtils.isLogged(this)) {
            menu.add(0, 2, 0, "Cerrar sesión (" + DispositivoUtils.getUsuarioNombre(this) + ")")
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        } else {
            menu.add(0, 2, 0, "Iniciar sesión")
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            boolean dark = !DispositivoUtils.isDarkMode(this);
            DispositivoUtils.setDarkMode(this, dark);
            AppCompatDelegate.setDefaultNightMode(
                    dark ? AppCompatDelegate.MODE_NIGHT_YES
                         : AppCompatDelegate.MODE_NIGHT_NO);
            recreate();
            return true;
        } else if (item.getItemId() == 2) {
            if (DispositivoUtils.isLogged(this)) {
                DispositivoUtils.logout(this);
                Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
                recreate();
            } else {
                startActivity(new Intent(this, ActividadLogin.class));
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refrescar favoritos al volver
        if (seccionFavoritos.getVisibility() == View.VISIBLE) cargarFavoritos();
        if (seccionMiNube.getVisibility() == View.VISIBLE) cargarMiNube();
    }
}
