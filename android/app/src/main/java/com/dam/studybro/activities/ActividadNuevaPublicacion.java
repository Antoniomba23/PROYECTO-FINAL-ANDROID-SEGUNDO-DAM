package com.dam.studybro.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.dam.studybro.R;
import com.dam.studybro.database.Asignatura;
import com.dam.studybro.database.BaseDatosApp;
import com.dam.studybro.database.Publicacion;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActividadNuevaPublicacion extends AppCompatActivity {

    private TextInputEditText etTitulo, etDescripcion;
    private AutoCompleteTextView spinnerTipo, spinnerAsignatura, spinnerCurso;
    private TextView tvNombreArchivo;
    private Uri archivoSeleccionado = null;

    private BaseDatosApp db;
    private ExecutorService executorService;
    private List<Asignatura> listaAsignaturas = new ArrayList<>();
    private int asignaturaPreseleccionada = -1;

    // Lanzador para elegir archivo del dispositivo
    private final ActivityResultLauncher<Intent> selectorArchivo = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    archivoSeleccionado = result.getData().getData();
                    String nombre = archivoSeleccionado.getLastPathSegment();
                    tvNombreArchivo.setText(nombre != null ? nombre : "Archivo seleccionado");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_nueva_publicacion);

        // ActionBar del tema
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Nueva Publicación");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Inicializar BD
        db = BaseDatosApp.getInstance(getApplicationContext());
        executorService = Executors.newSingleThreadExecutor();

        // Recibir asignatura preseleccionada del Intent (si viene desde una materia)
        asignaturaPreseleccionada = getIntent().getIntExtra("asignatura_id_preselected", -1);

        // Vincular campos
        etTitulo = findViewById(R.id.etTitle);
        etDescripcion = findViewById(R.id.etDescription);
        spinnerTipo = findViewById(R.id.spinnerType);
        spinnerAsignatura = findViewById(R.id.spinnerSubject);
        spinnerCurso = findViewById(R.id.spinnerCurso);
        tvNombreArchivo = findViewById(R.id.tvNombreArchivo);

        // Configurar tipo
        String[] tipos = {"Apunte", "Examen", "Duda", "Tarea"};
        ArrayAdapter<String> adapterTipo = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, tipos);
        spinnerTipo.setAdapter(adapterTipo);
        spinnerTipo.setText(tipos[0], false);

        // Configurar cursos académicos (últimos 3 años)
        String cursoActual = calcularAnioEscolar();
        int anioActual = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        String[] cursos = {
            cursoActual,
            (anioActual - 2) + "-" + (anioActual - 1),
            (anioActual - 3) + "-" + (anioActual - 2)
        };
        ArrayAdapter<String> adapterCurso = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, cursos);
        spinnerCurso.setAdapter(adapterCurso);
        spinnerCurso.setText(cursoActual, false); // Curso actual por defecto

        // Botón adjuntar archivo
        findViewById(R.id.btnAdjuntarArchivo).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*"); // Cualquier tipo de archivo
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            selectorArchivo.launch(intent);
        });

        // Cargar asignaturas desde BD
        cargarAsignaturas();

        // Botón publicar
        findViewById(R.id.btnPublish).setOnClickListener(v -> publicar());
    }

    private void cargarAsignaturas() {
        executorService.execute(() -> {
            listaAsignaturas = db.asignaturaDao().obtenerTodas();
            List<String> nombres = new ArrayList<>();
            String nombrePreseleccionado = null;

            for (Asignatura a : listaAsignaturas) {
                nombres.add(a.nombre);
                if (a.id == asignaturaPreseleccionada) {
                    nombrePreseleccionado = a.nombre;
                }
            }

            final String nombreFinal = nombrePreseleccionado;
            runOnUiThread(() -> {
                ArrayAdapter<String> adapterAsig = new ArrayAdapter<>(
                        this, android.R.layout.simple_dropdown_item_1line, nombres);
                spinnerAsignatura.setAdapter(adapterAsig);

                // Preseleccionar si viene del flujo de navegación
                if (nombreFinal != null) {
                    spinnerAsignatura.setText(nombreFinal, false);
                }
            });
        });
    }

    private void publicar() {
        String titulo = etTitulo.getText() != null ? etTitulo.getText().toString().trim() : "";
        String desc = etDescripcion.getText() != null ? etDescripcion.getText().toString().trim() : "";
        String tipo = spinnerTipo.getText().toString().trim();
        String nombreAsignatura = spinnerAsignatura.getText().toString().trim();

        if (titulo.isEmpty()) { etTitulo.setError("Escribe un título"); return; }
        if (desc.isEmpty())   { etDescripcion.setError("Escribe una descripción"); return; }
        if (tipo.isEmpty())   { Toast.makeText(this, "Elige el tipo", Toast.LENGTH_SHORT).show(); return; }
        if (nombreAsignatura.isEmpty()) { Toast.makeText(this, "Elige una asignatura", Toast.LENGTH_SHORT).show(); return; }

        // Deshabilitar botón mientras se procesa
        findViewById(R.id.btnPublish).setEnabled(false);

        // Si hay archivo → subirlo primero a Supabase Storage; si no → publicar directamente
        if (archivoSeleccionado != null) {
            subirArchivoYPublicar(titulo, desc, tipo, nombreAsignatura);
        } else {
            guardarPublicacionEnBD(titulo, desc, tipo, nombreAsignatura, null);
        }
    }

    /** Sube el archivo a Supabase Storage y, al terminar, guarda la publicación en Room */
    private void subirArchivoYPublicar(String titulo, String desc, String tipo, String nombreAsig) {
        try {
            // Leer token de sesión guardado en el login
            String token = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE)
                    .getString("supabase_token", "");

            // Preparar el archivo como multipart
            String mimeType = getContentResolver().getType(archivoSeleccionado);
            if (mimeType == null) mimeType = "application/octet-stream";

            java.io.InputStream stream = getContentResolver().openInputStream(archivoSeleccionado);
            byte[] bytes = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bytes = stream != null ? stream.readAllBytes() : new byte[0];
            }
            if (stream != null) stream.close();

            okhttp3.RequestBody cuerpoArchivo = okhttp3.RequestBody.create(
                    bytes, okhttp3.MediaType.parse(mimeType));

            // Nombre único: timestamp + nombre original
            String nombreArchivo = System.currentTimeMillis() + "_" +
                    archivoSeleccionado.getLastPathSegment();
            okhttp3.MultipartBody.Part parte = okhttp3.MultipartBody.Part
                    .createFormData("file", nombreArchivo, cuerpoArchivo);

            // Llamada a Supabase Storage
            com.dam.studybro.supabase.ServicioStorage storage = com.dam.studybro.supabase.ClienteSupabase.getStorage();
            storage.subirArchivo(
                    "Bearer " + token,
                    com.dam.studybro.supabase.ClienteSupabase.API_KEY,
                    com.dam.studybro.supabase.ServicioStorage.BUCKET,
                    nombreArchivo,
                    parte
            ).enqueue(new retrofit2.Callback<com.dam.studybro.supabase.ServicioStorage.RespuestaStorage>() {
                @Override
                public void onResponse(retrofit2.Call<com.dam.studybro.supabase.ServicioStorage.RespuestaStorage> call,
                                       retrofit2.Response<com.dam.studybro.supabase.ServicioStorage.RespuestaStorage> response) {
                    String urlArchivo = null;
                    if (response.isSuccessful()) {
                        urlArchivo = com.dam.studybro.supabase.ServicioStorage.obtenerUrlPublica(nombreArchivo);
                    } else {
                        Toast.makeText(ActividadNuevaPublicacion.this,
                                "Aviso: no se pudo subir el archivo", Toast.LENGTH_SHORT).show();
                    }
                    guardarPublicacionEnBD(titulo, desc, tipo, nombreAsig, urlArchivo);
                }

                @Override
                public void onFailure(retrofit2.Call<com.dam.studybro.supabase.ServicioStorage.RespuestaStorage> call, Throwable t) {
                    Toast.makeText(ActividadNuevaPublicacion.this,
                            "Sin conexión, publicando sin archivo", Toast.LENGTH_SHORT).show();
                    guardarPublicacionEnBD(titulo, desc, tipo, nombreAsig, null);
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error al leer el archivo", Toast.LENGTH_SHORT).show();
            findViewById(R.id.btnPublish).setEnabled(true);
        }
    }

    /** Guarda la publicación en la base de datos local (Room) */
    private void guardarPublicacionEnBD(String titulo, String desc, String tipo,
                                         String nombreAsig, String urlArchivo) {
        // Obtener el email/id del usuario logueado desde SharedPreferences
        String usuarioId = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE)
                .getString("email_usuario", "anonimo");

        executorService.execute(() -> {
            int asignaturaId = -1;
            for (Asignatura a : listaAsignaturas) {
                if (a.nombre.equals(nombreAsig)) { asignaturaId = a.id; break; }
            }
            if (asignaturaId == -1) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Asignatura no válida", Toast.LENGTH_SHORT).show();
                    findViewById(R.id.btnPublish).setEnabled(true);
                });
                return;
            }

            Publicacion pub = new Publicacion();
            pub.titulo       = titulo;
            pub.descripcion  = desc;
            pub.tipo         = tipo.toUpperCase();
            pub.asignaturaId = asignaturaId;
            pub.usuarioId    = usuarioId;  // Email/UUID de Supabase (String)
            pub.fechaSubida  = System.currentTimeMillis();
            pub.anioEscolar  = spinnerCurso.getText().toString();
            pub.archivoUrl   = urlArchivo;  // URL de Supabase Storage (puede ser null)

            db.publicacionDao().insertar(pub);

            runOnUiThread(() -> {
                Toast.makeText(this, urlArchivo != null
                        ? "¡Publicado con archivo! "
                        : "¡Publicado! ", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private String calcularAnioEscolar() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int mes  = cal.get(java.util.Calendar.MONTH);
        int anio = cal.get(java.util.Calendar.YEAR);
        if (mes >= java.util.Calendar.SEPTEMBER) return anio + "-" + (anio + 1);
        else return (anio - 1) + "-" + anio;
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}

