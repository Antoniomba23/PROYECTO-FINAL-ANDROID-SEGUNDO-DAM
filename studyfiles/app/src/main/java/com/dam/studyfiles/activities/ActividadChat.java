package com.dam.studyfiles.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dam.studyfiles.R;
import com.dam.studyfiles.adapters.AdaptadorChat;
import com.dam.studyfiles.models.MensajeChat;
import com.dam.studyfiles.network.SupabaseClient;
import com.dam.studyfiles.utils.DispositivoUtils;
import com.dam.studyfiles.utils.GeminiChatHelper;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActividadChat extends AppCompatActivity {

    private RecyclerView rvChat;
    private EditText etMensaje;
    private ImageButton btnEnviar;
    private ProgressBar pbEscribiendo;
    private AdaptadorChat adaptador;
    private List<MensajeChat> historial = new ArrayList<>();
    private String usuarioId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Tutor Virtual");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        rvChat = findViewById(R.id.rvChat);
        etMensaje = findViewById(R.id.etMensaje);
        btnEnviar = findViewById(R.id.btnEnviar);
        pbEscribiendo = findViewById(R.id.pbEscribiendo);

        adaptador = new AdaptadorChat(historial);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Ayuda a que los mensajes salgan desde abajo
        rvChat.setLayoutManager(layoutManager);
        rvChat.setAdapter(adaptador);

        // Identificador (ID usuario o UUID)
        usuarioId = getIdentificador();

        cargarHistorial();

        btnEnviar.setOnClickListener(v -> enviarMensaje());
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_limpiar_chat) {
            confirmarLimpiarChat();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmarLimpiarChat() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Limpiar historial")
                .setMessage("¿Estás seguro de que quieres borrar toda la conversación con el tutor?")
                .setPositiveButton("Borrar", (dialog, which) -> limpiarHistorialChat())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void limpiarHistorialChat() {
        pbEscribiendo.setVisibility(View.VISIBLE);
        SupabaseClient.getChat().eliminarHistorial("eq." + usuarioId)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        pbEscribiendo.setVisibility(View.GONE);
                        if (response.isSuccessful()) {
                            historial.clear();
                            adaptador.actualizarMensajes(historial);
                            Toast.makeText(ActividadChat.this, "Historial borrado", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ActividadChat.this, "Error al borrar historial", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        pbEscribiendo.setVisibility(View.GONE);
                        Toast.makeText(ActividadChat.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getIdentificador() {
        if (DispositivoUtils.isLogged(this)) {
            return "user_" + DispositivoUtils.getUsuarioId(this);
        }
        return DispositivoUtils.getUUID(this);
    }

    private void cargarHistorial() {
        pbEscribiendo.setVisibility(View.VISIBLE);
        SupabaseClient.getChat().getHistorialChat("eq." + usuarioId)
                .enqueue(new Callback<List<MensajeChat>>() {
            @Override
            public void onResponse(Call<List<MensajeChat>> call, Response<List<MensajeChat>> response) {
                pbEscribiendo.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    historial.clear();
                    historial.addAll(response.body());
                    adaptador.actualizarMensajes(historial);
                    scrollToBottom();
                } else {
                    try {
                        String errorDetails = response.errorBody() != null ? response.errorBody().string() : "Error desconocido";
                        Toast.makeText(ActividadChat.this, "Error DB: " + response.code() + " - " + errorDetails, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(ActividadChat.this, "Error cargando historial", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<MensajeChat>> call, Throwable t) {
                pbEscribiendo.setVisibility(View.GONE);
                Toast.makeText(ActividadChat.this, "Sin conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enviarMensaje() {
        String texto = etMensaje.getText().toString().trim();
        if (texto.isEmpty()) return;

        etMensaje.setText("");
        btnEnviar.setEnabled(false);
        pbEscribiendo.setVisibility(View.VISIBLE);

        // 1. Crear y mostrar mensaje del usuario
        MensajeChat msgUser = new MensajeChat(usuarioId, "user", texto, System.currentTimeMillis());
        adaptador.agregarMensaje(msgUser);
        scrollToBottom();

        // 2. Guardar en Supabase
        guardarMensajeEnSupabase(msgUser);

        // 3. Enviar consulta al tutor (pasando el historial que teníamos antes de añadir el nuevo mensaje)
        GeminiChatHelper.enviarMensajeChat(historial, texto, new GeminiChatHelper.GeminiCallback() {
            @Override
            public void onSuccess(String result) {
                btnEnviar.setEnabled(true);
                pbEscribiendo.setVisibility(View.GONE);

                // 4. Crear y mostrar respuesta del tutor
                MensajeChat msgIA = new MensajeChat(usuarioId, "model", result, System.currentTimeMillis());
                adaptador.agregarMensaje(msgIA);
                scrollToBottom();

                // 5. Guardar respuesta en Supabase
                guardarMensajeEnSupabase(msgIA);
            }

            @Override
            public void onError(String error) {
                btnEnviar.setEnabled(true);
                pbEscribiendo.setVisibility(View.GONE);
                Toast.makeText(ActividadChat.this, "Error del Tutor: " + error, Toast.LENGTH_LONG).show();
                // Opcional: eliminar el mensaje fallido o mostrar aviso
            }
        });
    }

    private void guardarMensajeEnSupabase(MensajeChat msg) {
        SupabaseClient.getChat().insertarMensaje(msg).enqueue(new Callback<List<MensajeChat>>() {
            @Override
            public void onResponse(Call<List<MensajeChat>> call, Response<List<MensajeChat>> response) {
                // Silencioso si tiene éxito
            }

            @Override
            public void onFailure(Call<List<MensajeChat>> call, Throwable t) {
                // Podríamos mostrar un aviso de que no se sincronizó
            }
        });
    }

    private void scrollToBottom() {
        if (adaptador.getItemCount() > 0) {
            rvChat.scrollToPosition(adaptador.getItemCount() - 1);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
