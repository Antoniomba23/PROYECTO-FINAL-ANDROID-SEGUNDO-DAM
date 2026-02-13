package com.example.studybro;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<EventModel> CentrosOriginales = new ArrayList<>();
    ArrayList<EventModel> CentrosCopia = new ArrayList<>();
    EventsAdapter adaptador;

    private void filtrar(String texto) {
        try {
            CentrosOriginales.clear();
            if (texto.isEmpty()) {
                CentrosOriginales.addAll(CentrosCopia);
            } else {
                String query = texto.toLowerCase().trim();
                for (EventModel event : CentrosCopia) {
                    if (event.getNombreCentro() != null && event.getNombreCentro().toLowerCase().contains(query)) {
                        CentrosOriginales.add(event);
                    }
                }
            }
            if (adaptador != null) {
                adaptador.notifyDataSetChanged();
            }
        } catch (Exception e) {
            Log.e("FILTRO_ERROR", "Error al filtrar: " + e.getMessage());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        try {


            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_main);





            ImageButton imageButton = findViewById(R.id.iconoPersona);


            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(MainActivity.this, PerfilUsuario.class);
                    startActivity(intent);
                }
            });

            // Verifica que este ID exista en activity_main.xml
            if (findViewById(R.id.mainMenuPrincipal) != null) {
                ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainMenuPrincipal), (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                });
            }

            recyclerView = findViewById(R.id.historic_Events_Recycle);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            adaptador = new EventsAdapter(this, CentrosOriginales);
            recyclerView.setAdapter(adaptador);

            // Buscador
            TextInputEditText editText = findViewById(R.id.buscador);
            if (editText != null) {
                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        filtrar(s.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });
            }

            cargarDatos();

        } catch (Exception e) {
            Log.e("CRASH_ON_CREATE", e.getMessage());
            Toast.makeText(this, "Error al iniciar: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void cargarDatos() {
        ApiInterfaz apiInterface = ApiCliente.getClient().create(ApiInterfaz.class);
        Call<Centros> call = apiInterface.getCentros();

        call.enqueue(new Callback<Centros>() {
            @Override
            public void onResponse(Call<Centros> call, Response<Centros> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        Centros centrosListado = response.body();
                        if (centrosListado.centrosMadrid == null) return;

                        // Limpiamos de forma segura
                        ArrayList<EventModel> temp = new ArrayList<>();
                        int limite = Math.min(50, centrosListado.centrosMadrid.size());

                        for (int i = 0; i < limite; i++) {
                            Centros.CentroMadrid c = centrosListado.centrosMadrid.get(i);

                            String nom = c.nombreCentro != null ? c.nombreCentro : "Sin nombre";
                            String calle = (c.address != null && c.address.streetName != null) ? c.address.streetName : "S/N";


                            String org = "N/A", desc = "N/A";

                            if (c.organization != null) {
                                org = c.organization.organizationName != null ? c.organization.organizationName : "N/A";
                                desc = c.organization.organizationDesc != null ? c.organization.organizationDesc : "N/A";

                                if (org.toLowerCase().contains("public") || org.toLowerCase().contains("públic")) {
                                    org = "Público";
                                } else if (org.toLowerCase().contains("municipal") || org.toLowerCase().contains("madrid")) {
                                    org = "Público";
                                } else
                                    org = "Privado";


                                if (desc.toLowerCase().contains("bachillerato")) {
                                    desc = "Bachillerato";
                                } else if (desc.toLowerCase().contains("primaria")) {
                                    desc = "Primaria";
                                } else if (desc.toLowerCase().contains("infantil")) {
                                    desc = "infantil";
                                } else if (desc.toLowerCase().contains("uni")) {
                                    desc = "Universidad";
                                } else {
                                    desc = "insituto";


                                }
                            }

                            temp.add(new EventModel(nom, calle, desc, org));
                        }

                        // Actualizar listas y UI
                        CentrosOriginales.clear();
                        CentrosCopia.clear();
                        CentrosOriginales.addAll(temp);
                        CentrosCopia.addAll(temp);
                        adaptador.notifyDataSetChanged();

                    } catch (Exception e) {
                        Log.e("ERROR_PROCESANDO", e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<Centros> call, Throwable t) {
                Log.e("API_FAILURE", t.getMessage());
            }
        });
    }
}