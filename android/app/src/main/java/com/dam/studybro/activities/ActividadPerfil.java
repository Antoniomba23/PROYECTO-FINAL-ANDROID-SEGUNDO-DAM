package com.dam.studybro.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.dam.studybro.R;
import com.dam.studybro.adapters.AdaptadorPublicaciones;
import com.dam.studybro.database.BaseDatosApp;
import com.dam.studybro.database.Publicacion;
import com.dam.studybro.supabase.ClienteSupabase;
import com.dam.studybro.supabase.ServicioStorage;
import com.google.android.material.button.MaterialButton;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Perfil del usuario: datos reales desde SharedPreferences, foto de perfil editable
 * con galería del sistema, estadísticas y publicaciones propias.
 */
public class ActividadPerfil extends AppCompatActivity {

    private static final String PREFS = "MisPreferencias";
    private static final String KEY_AVATAR_URL = "avatar_url";

    private BaseDatosApp db;
    private ExecutorService executor;
    private AdaptadorPublicaciones adaptador;

    private ImageView ivAvatar;
    private String emailUsuario;

    // Lanzador para abrir la galería del sistema
    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(),
                    uri -> { if (uri != null) subirFotoPerfil(uri); });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_perfil);

        // Toolbar con botón atrás
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mi Perfil");
        }

        db       = BaseDatosApp.getInstance(getApplicationContext());
        executor = Executors.newSingleThreadExecutor();

        // ── Datos de SharedPreferences ─────────────────────────────────────────
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        emailUsuario          = prefs.getString("email_usuario", "Sin sesión");
        String centro         = prefs.getString("nombre_centro", "Sin centro asignado");
        boolean haySession    = prefs.getBoolean("sesion_iniciada", false);
        String avatarUrl      = prefs.getString(KEY_AVATAR_URL, null);
        String rolUsuario     = prefs.getString("rol_usuario", "ESTUDIANTE");

        if (!haySession) {
            Toast.makeText(this, "Inicia sesión para ver tu perfil", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, ActividadLogin.class));
            finish();
            return;
        }

        // Nombre: parte antes del @ capitalizada
        String nombre = emailUsuario.contains("@")
                ? emailUsuario.substring(0, emailUsuario.indexOf("@"))
                : emailUsuario;
        nombre = nombre.substring(0, 1).toUpperCase() + nombre.substring(1);

        // ── Vistas de texto ────────────────────────────────────────────────────
        ((TextView) findViewById(R.id.tvUsername)).setText(nombre);
        ((TextView) findViewById(R.id.tvEmail)).setText(emailUsuario);
        
        TextView tvCenter = findViewById(R.id.tvCenter);
        if ("ADMIN".equals(rolUsuario)) {
            tvCenter.setText("👑 Administrador Global");
        } else {
            tvCenter.setText(centro.isEmpty() ? "Sin centro asignado" : centro);
        }

        // ── Avatar: cargar foto si existe, hacer cliclable para cambiar ────────
        ivAvatar = findViewById(R.id.ivAvatar);
        cargarAvatar(avatarUrl);
        ivAvatar.setOnClickListener(v -> pickImage.launch("image/*"));

        // ── RecyclerView de mis publicaciones ──────────────────────────────────
        RecyclerView rv = findViewById(R.id.recyclerMyPosts);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adaptador = new AdaptadorPublicaciones(new ArrayList<>(), pub -> {
            Intent intent = new Intent(this, ActividadDetalle.class);
            intent.putExtra(ActividadDetalle.EXTRA_PUBLICACION_ID, pub.id);
            startActivity(intent);
        });
        rv.setAdapter(adaptador);

        // ── Cargar estadísticas y publicaciones en hilo de fondo ───────────────
        final String emailFinal = emailUsuario;
        executor.execute(() -> {
            List<Publicacion> misPublicaciones = db.publicacionDao().obtenerPorUsuario(emailFinal);
            int totalLikes = 0, totalFavs = 0;
            for (Publicacion pub : misPublicaciones) {
                totalLikes += db.interaccionDao().contarInteracciones(pub.id, "ME_GUSTA");
                totalFavs  += db.interaccionDao().contarInteracciones(pub.id, "GUARDADO");
            }
            final int likesFinales = totalLikes;
            final int favsFinales  = totalFavs;

            runOnUiThread(() -> {
                TextView tvPubs  = findViewById(R.id.tvStatPubs);
                TextView tvLikes = findViewById(R.id.tvStatLikes);
                TextView tvFavs  = findViewById(R.id.tvStatFavs);
                if (tvPubs  != null) tvPubs.setText(String.valueOf(misPublicaciones.size()));
                if (tvLikes != null) tvLikes.setText(String.valueOf(likesFinales));
                if (tvFavs  != null) tvFavs.setText(String.valueOf(favsFinales));

                adaptador.actualizarDatos(misPublicaciones);
                TextView tvNoPosts = findViewById(R.id.tvNoPosts);
                if (tvNoPosts != null)
                    tvNoPosts.setVisibility(misPublicaciones.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });

        // ── Botón Cerrar Sesión ────────────────────────────────────────────────
        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Cerrar sesión")
                        .setMessage("¿Quieres cerrar la sesión?")
                        .setPositiveButton("Sí", (d, w) -> {
                            getSharedPreferences(PREFS, MODE_PRIVATE).edit().clear().apply();
                            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(this, ActividadPrincipal.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        })
                        .setNegativeButton("Cancelar", null)
                        .show()
        );

        // ── Botón Nueva Publicación ────────────────────────────────────────────
        MaterialButton btnNuevaPublicacion = findViewById(R.id.btnNuevaPublicacion);
        if (btnNuevaPublicacion != null) {
            btnNuevaPublicacion.setOnClickListener(v ->
                    startActivity(new Intent(this, ActividadNuevaPublicacion.class)));
        }
    }

    /** Carga el avatar desde URL con Glide (forma circular) o muestra el icono por defecto. */
    private void cargarAvatar(String url) {
        if (url != null && !url.isEmpty()) {
            Glide.with(this)
                    .load(url)
                    .transform(new CircleCrop())
                    .placeholder(R.drawable.ic_person)
                    .into(ivAvatar);
        } else {
            ivAvatar.setImageResource(R.drawable.ic_person);
        }
    }

    /** Sube la imagen al bucket 'avatares' de Supabase Storage y guarda la URL en prefs. */
    private void subirFotoPerfil(Uri uri) {
        executor.execute(() -> {
            try {
                InputStream is = getContentResolver().openInputStream(uri);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[4096];
                int n;
                while ((n = is.read(buf)) != -1) baos.write(buf, 0, n);
                is.close();
                byte[] bytes = baos.toByteArray();

                String nombreArchivo = "avatar_" + emailUsuario.replace("@", "_at_") + ".jpg";

                okhttp3.MultipartBody.Part part = okhttp3.MultipartBody.Part.createFormData(
                        "file", nombreArchivo,
                        RequestBody.create(okhttp3.MediaType.parse("image/jpeg"), bytes));

                // Recuperar access_token de prefs para la cabecera Bearer
                SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
                String token = prefs.getString("access_token", "");
                String bearer = "Bearer " + token;

                Call<ServicioStorage.RespuestaStorage> call =
                        ClienteSupabase.getStorage().subirArchivo(
                                bearer,
                                ClienteSupabase.API_KEY,
                                "avatares",
                                nombreArchivo,
                                part);

                retrofit2.Response<ServicioStorage.RespuestaStorage> resp = call.execute();
                if (resp.isSuccessful()) {
                    String urlPublica = ClienteSupabase.URL_BASE
                            + "storage/v1/object/public/avatares/" + nombreArchivo;
                    prefs.edit().putString(KEY_AVATAR_URL, urlPublica).apply();
                    runOnUiThread(() -> cargarAvatar(urlPublica));
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Error al subir la foto", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "No se pudo subir la foto", Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
