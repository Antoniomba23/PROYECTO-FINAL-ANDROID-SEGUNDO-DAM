package com.dam.studybro.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dam.studybro.R;
import com.dam.studybro.adapters.AdaptadorCentros;
import com.dam.studybro.database.BaseDatosApp;
import com.dam.studybro.database.Centro;
import com.dam.studybro.modelos.CentroMadrid;
import com.dam.studybro.modelos.RespuestaDatosMadrid;
import com.dam.studybro.red.ClienteApi;
import com.dam.studybro.red.ServicioApi;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity Principal — Lista de centros con Navigation Drawer (menú hamburguesa).
 */
public class ActividadPrincipal extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView recyclerView;
    private AdaptadorCentros adaptador;
    private BaseDatosApp db;
    private ExecutorService executorService;        // lecturas / seeder
    private ExecutorService executorEscritura;      // escrituras de API

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_principal);

        // Executors primero; la BD se inicializa en background para no bloquear el hilo principal
        executorService   = Executors.newSingleThreadExecutor();
        executorEscritura = Executors.newSingleThreadExecutor();

        //  Toolbar con botón hamburguesa
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout   = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        SharedPreferences prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        boolean sesionIniciada  = prefs.getBoolean("sesion_iniciada", false);
        String  supabaseToken   = prefs.getString("supabase_token", "");

        // Limpiar sesión falsa heredada de Room (sin token Supabase)
        if (sesionIniciada && supabaseToken.isEmpty()) {
            prefs.edit().clear().apply();
            sesionIniciada = false;
        }

        String email = prefs.getString("email_usuario", "");
        actualizarDrawer(sesionIniciada, email);


        recyclerView = findViewById(R.id.recyclerViewCenters);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adaptador = new AdaptadorCentros(new ArrayList<>(), centro -> {
            Intent intent = new Intent(this, ActividadPerfilCentro.class);
            intent.putExtra("centro_id", centro.id);
            startActivity(intent);
        });
        recyclerView.setAdapter(adaptador);

        // Buscador
        com.google.android.material.textfield.TextInputEditText etSearch = findViewById(R.id.etSearch);

        // Buscador con Debouncing para evitar ANR con +2000 centros
        android.os.Handler searchHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        Runnable searchRunnable = () -> {
            String text = etSearch.getText().toString();
            adaptador.filtrar(text);
        };

        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchHandler.removeCallbacks(searchRunnable);
                searchHandler.postDelayed(searchRunnable, 300); // Esperar 300ms antes de filtrar
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });


        // 1. La BD se inicializa en background (el primer acceso crea/migra el archivo .db)
        executorService.execute(() -> {
            db = BaseDatosApp.getInstance(getApplicationContext());
            com.dam.studybro.database.DatabaseSeeder.sembrarDatos(db);
            List<Centro> centros = db.centroDao().obtenerTodos();
            runOnUiThread(() -> {
                adaptador.actualizarDatos(centros);
                // 2. Petición a la API solo cuando la UI ya tiene datos
                obtenerDatosDeApi();
            });
        });
    }

    /** Actualiza la cabecera del drawer y muestra/oculta Login o Logout */
    private void actualizarDrawer(boolean sesionIniciada, String email) {
        View headerView = navigationView.getHeaderView(0);
        TextView tvEmail  = headerView.findViewById(R.id.tvNavEmail);
        TextView tvCentro = headerView.findViewById(R.id.tvNavCentro);

        if (sesionIniciada) {
            tvEmail.setText(email);
            String nombreCentro = getSharedPreferences("MisPreferencias", MODE_PRIVATE)
                    .getString("nombre_centro", "");
            tvCentro.setText(nombreCentro.isEmpty() ? "Sin centro asignado" : "📍 " + nombreCentro);
        } else {
            tvEmail.setText("Modo Invitado");
            tvCentro.setText("");
        }

        navigationView.getMenu().findItem(R.id.nav_login).setVisible(!sesionIniciada);
        navigationView.getMenu().findItem(R.id.nav_logout).setVisible(sesionIniciada);
        navigationView.getMenu().findItem(R.id.nav_perfil).setVisible(sesionIniciada);
        navigationView.getMenu().findItem(R.id.nav_cambiar_centro).setVisible(sesionIniciada);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_inicio) {
            // Ya estamos aquí, sólo cierra el drawer
        } else if (id == R.id.nav_perfil) {
            startActivity(new Intent(this, ActividadPerfil.class));
        } else if (id == R.id.nav_cambiar_centro) {
            startActivity(new Intent(this, ActividadSeleccionarCentro.class));
        } else if (id == R.id.nav_login) {
            startActivity(new Intent(this, ActividadLogin.class));
        } else if (id == R.id.nav_logout) {
            // Cerrar sesión
            getSharedPreferences("MisPreferencias", MODE_PRIVATE).edit().clear().apply();
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
            recreate(); // Refresca la actividad
        }

        drawerLayout.closeDrawers();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refrescar drawer al volver del login
        SharedPreferences prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        boolean sesionIniciada  = prefs.getBoolean("sesion_iniciada", false);
        String  email           = prefs.getString("email_usuario", "");
        actualizarDrawer(sesionIniciada, email);
    }


    // CARGA DE DATOS


    private void cargarDatosLocales() {
        executorService.execute(() -> {
            List<Centro> centros = db.centroDao().obtenerTodos();
            runOnUiThread(() -> adaptador.actualizarDatos(centros));
        });
    }

    private void obtenerDatosDeApi() {
        ServicioApi servicio = ClienteApi.obtenerInstancia();
        Call<RespuestaDatosMadrid> llamada = servicio.obtenerCentrosEducativos();
        llamada.enqueue(new Callback<RespuestaDatosMadrid>() {
            @Override
            public void onResponse(Call<RespuestaDatosMadrid> call, Response<RespuestaDatosMadrid> response) {
                if (response.isSuccessful() && response.body() != null && response.body().graph != null) {
                    guardarDatosEnBaseDeDatos(response.body().graph);
                }
            }
            @Override
            public void onFailure(Call<RespuestaDatosMadrid> call, Throwable t) {
                Log.e("API_ERROR", "Fallo red: " + t.getMessage());
            }
        });
    }

    private void guardarDatosEnBaseDeDatos(List<CentroMadrid> centrosApi) {
        // Usamos executorEscritura para no bloquear el executor de lectura/seeder
        executorEscritura.execute(() -> {
            // 1. Obtener todos los códigos ya existentes para evitar duplicados eficientemente
            List<String> codigosExistentesList = db.centroDao().obtenerTodosLosCodigosApi();
            java.util.HashSet<String> codigosExistentes = new java.util.HashSet<>(codigosExistentesList);

            List<Centro> nuevos = new ArrayList<>();
            for (CentroMadrid cApi : centrosApi) {
                // Comprobación rápida en memoria (O(1)) en lugar de consulta a BD (O(logN))
                if (codigosExistentes.contains(cApi.id)) continue;

                Centro nuevo = new Centro();
                nuevo.nombre          = cApi.title != null ? cApi.title : "Sin nombre";
                nuevo.codigoApi       = cApi.id;
                nuevo.valoracionMedia = 0.0f;
                nuevo.imagenUrl       = "https://picsum.photos/seed/" + cApi.id + "/400/200";
                
                if (cApi.address != null) {
                    nuevo.direccion = cApi.address.streetAddress;
                    nuevo.ciudad    = cApi.address.locality;
                }

                if (cApi.organization != null) {
                    nuevo.descripcion   = cApi.organization.organizationDesc;
                    nuevo.horario       = cApi.organization.schedule;
                    nuevo.accesibilidad = cApi.organization.accesibility;
                    nuevo.servicios     = cApi.organization.services;
                    nuevo.webUrl        = cApi.relation != null ? cApi.relation : "https://www.madrid.es";
                }
                
                nuevo.urlDetalle = cApi.relation;
                
                nuevos.add(nuevo);
                // Evitamos que 'nuevos' crezca infinitamente si hubiera un error en la API
                // pero permitimos cargar todo el dataset normal (+2000)
            }

            // Inserción masiva en una sola transacción (muy rápido)
            if (!nuevos.isEmpty()) {
                db.centroDao().insertarLista(nuevos);
                Log.d("API_SYNC", "Insertados " + nuevos.size() + " centros nuevos");
            }

            cargarDatosLocales();
        });
    }
}
