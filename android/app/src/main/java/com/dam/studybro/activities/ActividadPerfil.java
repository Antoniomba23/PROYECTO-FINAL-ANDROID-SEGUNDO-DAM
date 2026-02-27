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
import android.widget.ProgressBar;

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

import com.dam.studybro.network.SupabaseApi;
import com.dam.studybro.network.SupabaseClient;

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
    private String filtroActual = "MIS_PUBS"; // MIS_PUBS, UTILES, FAVS

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
        if (nombre != null && !nombre.isEmpty()) {
            nombre = nombre.substring(0, 1).toUpperCase() + nombre.substring(1);
        }

        // ── Vistas de texto ────────────────────────────────────────────────────
        ((TextView) findViewById(R.id.tvUsername)).setText(nombre);
        ((TextView) findViewById(R.id.tvEmail)).setText(emailUsuario);
        
        TextView tvCenter = findViewById(R.id.tvCenter);
        if ("ADMIN".equals(rolUsuario)) {
            tvCenter.setText("👑 Administrador Global");
            
            // Ocultar vistas irrelevantes para administradores (estadísticas, botón publicar, lista de posts)
            View vDividerStats = findViewById(R.id.vDividerStats);
            View llStats = findViewById(R.id.llStats);
            View btnNuevaPublicacion = findViewById(R.id.btnNuevaPublicacion);
            View tvLabelMisPubs = findViewById(R.id.tvLabelMisPubs);
            RecyclerView rvPosts = findViewById(R.id.recyclerMyPosts);
            View tvNoPosts = findViewById(R.id.tvNoPosts);

            if (vDividerStats != null) vDividerStats.setVisibility(View.GONE);
            if (llStats != null) llStats.setVisibility(View.GONE);
            if (btnNuevaPublicacion != null) btnNuevaPublicacion.setVisibility(View.GONE);
            if (tvLabelMisPubs != null) tvLabelMisPubs.setVisibility(View.GONE);
            if (rvPosts != null) rvPosts.setVisibility(View.GONE);
            if (tvNoPosts != null) tvNoPosts.setVisibility(View.GONE);

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

        // ── Cargar estadísticas y publicaciones en hilo de fondo (sólo para estudiantes) ───────────────
        if (!"ADMIN".equals(rolUsuario)) {
            cargarDatosCloud();

            // Configurar Listeners de Filtros
            findViewById(R.id.llStatPubs).setOnClickListener(v -> cambiarFiltro("MIS_PUBS"));
            findViewById(R.id.llStatLikes).setOnClickListener(v -> cambiarFiltro("UTILES"));
            findViewById(R.id.llStatFavs).setOnClickListener(v -> cambiarFiltro("FAVS"));
        }

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

        // ── Navegación ──
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        com.dam.studybro.utils.NavigationHelper.setupBottomNavigation(this, bottomNav);

        // ── Botón Nueva Publicación ────────────────────────────────────────────
        MaterialButton btnNuevaPublicacion = findViewById(R.id.btnNuevaPublicacion);
        if (btnNuevaPublicacion != null) {
            btnNuevaPublicacion.setOnClickListener(v ->
                    startActivity(new Intent(this, ActividadNuevaPublicacion.class)));
        }

        // ── Botón Asistente IA ──────────────────────────────────────────────────
        com.google.android.material.floatingactionbutton.FloatingActionButton fabGemini = findViewById(R.id.fabGemini);
        if (fabGemini != null) {
            fabGemini.setOnClickListener(v -> {
                ProgressBar progressBar = findViewById(R.id.progressBar);
                if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
                fabGemini.setEnabled(false);

                String contexto = "El usuario está en su perfil personal. Tiene " + (adaptador != null ? adaptador.getItemCount() : 0) + " publicaciones visibles.";
                com.dam.studybro.utils.GeminiHelper.pedirAyudaGeneral(contexto, "Consejos para mejorar mi perfil de estudiante o dudas sobre el app", 
                    new com.dam.studybro.utils.GeminiHelper.GeminiCallback() {
                        @Override
                        public void onSuccess(String result) {
                            if (progressBar != null) progressBar.setVisibility(View.GONE);
                            fabGemini.setEnabled(true);
                            mostrarDialogoAI(result);
                        }
                        @Override
                        public void onError(String error) {
                            if (progressBar != null) progressBar.setVisibility(View.GONE);
                            fabGemini.setEnabled(true);
                            Toast.makeText(ActividadPerfil.this, error, Toast.LENGTH_SHORT).show();
                        }
                    });
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDatosCloud();
    }

    private void mostrarDialogoAI(String result) {
        String textoLimpio = result.replace("**", "").replace("*", "•");
        new AlertDialog.Builder(this)
                .setTitle("✨ Asistente IA")
                .setMessage(textoLimpio)
                .setPositiveButton("Entendido", null)
                .show();
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

                // Recuperar token de prefs (se guardó como 'supabase_token' en login)
                SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
                String token = prefs.getString("supabase_token", "");
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

    private void cambiarFiltro(String nuevoFiltro) {
        this.filtroActual = nuevoFiltro;
        // Cambiar títulos visuales
        TextView tvLabel = findViewById(R.id.tvLabelMisPubs);
        switch (nuevoFiltro) {
            case "MIS_PUBS": tvLabel.setText("Mis publicaciones"); break;
            case "UTILES":   tvLabel.setText("Archivos Útiles"); break;
            case "FAVS":     tvLabel.setText("Mis Favoritos"); break;
        }
        actualizarListaSegunFiltro();
    }

    private void cargarDatosCloud() {
        com.dam.studybro.network.SupabaseApi api = com.dam.studybro.network.SupabaseClient.getClient().create(com.dam.studybro.network.SupabaseApi.class);
        
        // 1. Mis publicaciones
        String queryId = "eq." + emailUsuario.toLowerCase();
        api.getPublicacionesPorUsuario(queryId).enqueue(new retrofit2.Callback<List<Publicacion>>() {
            @Override
            public void onResponse(Call<List<Publicacion>> call, Response<List<Publicacion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Publicacion> posts = response.body();
                    TextView tvPubs = findViewById(R.id.tvStatPubs);
                    if (tvPubs != null) tvPubs.setText(String.valueOf(posts.size()));
                    if (filtroActual.equals("MIS_PUBS")) {
                        adaptador.actualizarDatos(posts);
                        toggleEmptyView(posts.isEmpty());
                    }
                }
            }
            @Override
            public void onFailure(Call<List<Publicacion>> call, Throwable t) {}
        });

        // 2. Útiles (Interacciones locales + Supabase IDs)
        executor.execute(() -> {
            List<Integer> idsUtiles = db.interaccionDao().obtenerIdsInteracciones(emailUsuario, "ME_GUSTA");
            runOnUiThread(() -> {
                TextView tvLikes = findViewById(R.id.tvStatLikes);
                if (tvLikes != null) tvLikes.setText(String.valueOf(idsUtiles.size()));
                if (filtroActual.equals("UTILES")) cargarPorIds(idsUtiles);
            });
        });

        // 3. Favoritos (Interacciones locales + Supabase IDs)
        executor.execute(() -> {
            List<Integer> idsFavs = db.interaccionDao().obtenerIdsInteracciones(emailUsuario, "GUARDADO");
            runOnUiThread(() -> {
                TextView tvFavs = findViewById(R.id.tvStatFavs);
                if (tvFavs != null) tvFavs.setText(String.valueOf(idsFavs.size()));
                if (filtroActual.equals("FAVS")) cargarPorIds(idsFavs);
            });
        });
    }

    private void cargarPorIds(List<Integer> ids) {
        if (ids.isEmpty()) {
            adaptador.actualizarDatos(new ArrayList<>());
            toggleEmptyView(true);
            return;
        }
        StringBuilder sb = new StringBuilder("in.(");
        for (int i = 0; i < ids.size(); i++) {
            sb.append(ids.get(i));
            if (i < ids.size() - 1) sb.append(",");
        }
        sb.append(")");

        com.dam.studybro.network.SupabaseApi api = com.dam.studybro.network.SupabaseClient.getClient().create(com.dam.studybro.network.SupabaseApi.class);
        api.getPublicacionesPorIds(sb.toString()).enqueue(new retrofit2.Callback<List<Publicacion>>() {
            @Override
            public void onResponse(Call<List<Publicacion>> call, Response<List<Publicacion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adaptador.actualizarDatos(response.body());
                    toggleEmptyView(response.body().isEmpty());
                }
            }
            @Override
            public void onFailure(Call<List<Publicacion>> call, Throwable t) {}
        });
    }

    private void toggleEmptyView(boolean empty) {
        TextView tvNoPosts = findViewById(R.id.tvNoPosts);
        if (tvNoPosts != null) {
            tvNoPosts.setVisibility(empty ? View.VISIBLE : View.GONE);
            if (empty) {
                if (filtroActual.equals("UTILES")) tvNoPosts.setText("No tienes archivos marcados como útiles 💡");
                else if (filtroActual.equals("FAVS")) tvNoPosts.setText("No tienes archivos favoritos ⭐️");
                else tvNoPosts.setText("Aún no has publicado nada 📭");
            }
        }
    }

    private void actualizarListaSegunFiltro() {
        cargarDatosCloud();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
