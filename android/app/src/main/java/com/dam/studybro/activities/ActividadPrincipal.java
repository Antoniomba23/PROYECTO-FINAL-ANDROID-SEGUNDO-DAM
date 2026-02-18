package com.dam.studybro.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.dam.studybro.R;
import com.dam.studybro.adapters.AdaptadorCentros;
import com.dam.studybro.database.BaseDatosApp;
import com.dam.studybro.database.Centro;
import com.dam.studybro.modelos.CentroMadrid;
import com.dam.studybro.modelos.RespuestaDatosMadrid;
import com.dam.studybro.red.ClienteApi;
import com.dam.studybro.red.ServicioApi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity Principal - Pantalla principal con lista de centros
 * Responsable: Jorge
 */
public class ActividadPrincipal extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdaptadorCentros adaptador;
    private BaseDatosApp db;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_principal);

        // Inicializar Base de Datos y Executor
        db = Room.databaseBuilder(getApplicationContext(),
                BaseDatosApp.class, "studybro-db").build();
        executorService = Executors.newSingleThreadExecutor();

        // Check Session (Guest vs Student)
        android.content.SharedPreferences prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        boolean isGuest = prefs.getInt("centro_id", -1) == -1;
        String userEmail = prefs.getString("email_usuario", "Invitado");

        if (isGuest) {
            getSupportActionBar().setSubtitle("Modo Invitado");
            // Ocultar botones de "Mi Perfil" si fuera necesario, para demo lo dejamos
        } else {
            getSupportActionBar().setSubtitle(userEmail);
        }

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.recyclerViewCenters); // Corregido ID
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adaptador = new AdaptadorCentros(new ArrayList<>(), centro -> {
            Intent intent = new Intent(ActividadPrincipal.this, ActividadPerfilCentro.class);
            intent.putExtra("centro_id", centro.id); // Pasamos ID interno
            startActivity(intent);
        });
        recyclerView.setAdapter(adaptador);

        // BUSCADOR UNIVERSAL (Nivel UI)
        com.google.android.material.textfield.TextInputEditText etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filtrar lista
                adaptador.filtrar(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Cargar datos (Primero DB, luego API)
        com.dam.studybro.database.DatabaseSeeder.sembrarDatos(db);
        cargarDatosLocales();
        obtenerDatosDeApi();

        // Configurar Botones
        // Botón Ir a Publicaciones (Demo)
        findViewById(R.id.btnIrPublicaciones).setOnClickListener(v -> {
             startActivity(new Intent(this, ActividadPublicaciones.class));
        });

        // Botón Perfil (Toolbar)
        findViewById(R.id.btnProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, ActividadPerfil.class));
        });
    }

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
                } else {
                    Toast.makeText(ActividadPrincipal.this, "Error en respuesta API", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RespuestaDatosMadrid> call, Throwable t) {
                Log.e("API_ERROR", "Fallo red: " + t.getMessage());
                Toast.makeText(ActividadPrincipal.this, "Error de red al cargar centros", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void guardarDatosEnBaseDeDatos(List<CentroMadrid> centrosApi) {
        executorService.execute(() -> {
            for (CentroMadrid cApi : centrosApi) {
                // Verificar si ya existe para no duplicar (por código API)
                Centro existente = db.centroDao().obtenerPorCodigoApi(cApi.id);
                if (existente == null) {
                    Centro nuevo = new Centro();
                    nuevo.nombre = cApi.title != null ? cApi.title : "Sin nombre";
                    nuevo.codigoApi = cApi.id;
                    nuevo.valoracionMedia = 0.0f; // Inicial
                    
                    if (cApi.address != null) {
                        nuevo.direccion = cApi.address.streetAddress;
                        nuevo.ciudad = cApi.address.locality;
                    }
                    if (cApi.organization != null) {
                        nuevo.webUrl = "https://www.madrid.es"; // Default o extraer si hay
                    }
                    
                    db.centroDao().insertar(nuevo);
                }
            }
            // Recargar UI con nuevos datos
            cargarDatosLocales();
        });
    }
}
