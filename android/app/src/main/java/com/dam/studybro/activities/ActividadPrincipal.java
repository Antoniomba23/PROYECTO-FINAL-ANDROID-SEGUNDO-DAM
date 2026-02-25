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
    private ExecutorService executorService;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_principal);

        // BD y Executor
        db = BaseDatosApp.getInstance(getApplicationContext());
        executorService = Executors.newSingleThreadExecutor();

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
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adaptador.filtrar(s.toString());
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });


        // Cargar datos
        executorService.execute(() -> {
            com.dam.studybro.database.DatabaseSeeder.sembrarDatos(db);
            runOnUiThread(() -> {
                cargarDatosLocales();
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
        } else if (id == R.id.nav_publicaciones) {
            startActivity(new Intent(this, ActividadPublicaciones.class));
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
        executorService.execute(() -> {
            // Limitar a 50 centros para no saturar el emulador
            int limite = Math.min(centrosApi.size(), 50);

            List<Centro> nuevos = new ArrayList<>();
            for (int i = 0; i < limite; i++) {
                CentroMadrid cApi = centrosApi.get(i);
                if (db.centroDao().obtenerPorCodigoApi(cApi.id) != null) continue;

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
                    nuevo.webUrl = "https://www.madrid.es";
                }
                nuevos.add(nuevo);
            }

            // Inserción masiva en una sola transacción
            if (!nuevos.isEmpty()) {
                db.centroDao().insertarLista(nuevos);
            }

            cargarDatosLocales();
        });
    }
}
