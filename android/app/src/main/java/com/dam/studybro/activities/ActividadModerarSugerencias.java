package com.dam.studybro.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dam.studybro.R;
import com.dam.studybro.adapters.AdaptadorSugerenciaEspecialidad;
import com.dam.studybro.adapters.AdaptadorSugerenciaMateria;
import com.dam.studybro.database.Asignatura;
import com.dam.studybro.database.BaseDatosApp;
import com.dam.studybro.database.CentroEspecialidad;
import com.dam.studybro.database.Especialidad;
import com.dam.studybro.database.SugerenciaEspecialidad;
import com.dam.studybro.database.SugerenciaMateria;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActividadModerarSugerencias extends AppCompatActivity {

    private RecyclerView rvEspecialidades, rvMaterias;
    private AdaptadorSugerenciaEspecialidad adpEspecialidades;
    private AdaptadorSugerenciaMateria adpMaterias;
    private BaseDatosApp db;
    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_moderar_sugerencias);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Moderar Sugerencias");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = BaseDatosApp.getInstance(this);
        executor = Executors.newSingleThreadExecutor();

        rvEspecialidades = findViewById(R.id.rvSugerenciasEspecialidad);
        rvMaterias = findViewById(R.id.rvSugerenciasMateria);

        rvEspecialidades.setLayoutManager(new LinearLayoutManager(this));
        rvMaterias.setLayoutManager(new LinearLayoutManager(this));

        // Inicializar adaptadores vacíos
        adpEspecialidades = new AdaptadorSugerenciaEspecialidad(null, new AdaptadorSugerenciaEspecialidad.OnSugerenciaClickListener() {
            @Override
            public void onAprobar(SugerenciaEspecialidad sugerencia) {
                aprobarEspecialidad(sugerencia);
            }

            @Override
            public void onRechazar(SugerenciaEspecialidad sugerencia) {
                rechazarEspecialidad(sugerencia);
            }
        });
        rvEspecialidades.setAdapter(adpEspecialidades);

        adpMaterias = new AdaptadorSugerenciaMateria(null, new AdaptadorSugerenciaMateria.OnSugerenciaClickListener() {
            @Override
            public void onAprobar(SugerenciaMateria sugerencia) {
                aprobarMateria(sugerencia);
            }

            @Override
            public void onRechazar(SugerenciaMateria sugerencia) {
                rechazarMateria(sugerencia);
            }
        });
        rvMaterias.setAdapter(adpMaterias);

        cargarDatos();
    }

    private void cargarDatos() {
        executor.execute(() -> {
            List<SugerenciaEspecialidad> listaEsp = db.sugerenciaEspecialidadDao().obtenerTodas();
            List<SugerenciaMateria> listaMat = db.sugerenciaMateriaDao().obtenerTodas();

            runOnUiThread(() -> {
                adpEspecialidades.actualizarLista(listaEsp);
                adpMaterias.actualizarLista(listaMat);
            });
        });
    }

    private void aprobarEspecialidad(SugerenciaEspecialidad sugerencia) {
        executor.execute(() -> {
            // 1. Insertar en la tabla real de Especialidades
            Especialidad nuevaReal = new Especialidad("SUGERIDA_APROBADA", sugerencia.nombreSugerido);
            long idReal = db.especialidadDao().insertar(nuevaReal);

            // 2. Vincular automáticamente al centro que la solicitó
            db.centroEspecialidadDao().insertar(new CentroEspecialidad(sugerencia.centroId, (int) idReal));

            // 3. Borrar de sugerencias pendientes
            db.sugerenciaEspecialidadDao().eliminarPorId(sugerencia.id);

            runOnUiThread(() -> {
                Toast.makeText(this, "Especialidad aprobada e integrada", Toast.LENGTH_SHORT).show();
                cargarDatos();
            });
        });
    }

    private void rechazarEspecialidad(SugerenciaEspecialidad sugerencia) {
        executor.execute(() -> {
            db.sugerenciaEspecialidadDao().eliminarPorId(sugerencia.id);
            runOnUiThread(() -> {
                Toast.makeText(this, "Sugerencia descartada", Toast.LENGTH_SHORT).show();
                cargarDatos();
            });
        });
    }

    private void aprobarMateria(SugerenciaMateria sugerencia) {
        executor.execute(() -> {
            // Nota: Como los alumnos solicitan una materia desde dentro de un "Centro", 
            // no sabemos exactamente a qué Especialidad dentro de ese centro la quieren añadir 
            // a menos que modificásemos todo el formulario estudiante.
            // Para simplificar, la asignaremos a una Especialidad genérica "Materias Extra" 
            // o a la primera especialidad del centro.
            
            // Buscar la primera especialidad de este centro para asignarle la materia
            List<Especialidad> especialidadesDelCentro = db.centroEspecialidadDao().obtenerPorCentro(sugerencia.centroId);
            
            if (especialidadesDelCentro.isEmpty()) {
                runOnUiThread(() -> Toast.makeText(this, "El centro no tiene especialidades. Imposible asignar materia.", Toast.LENGTH_LONG).show());
                return;
            }
            
            int especialidadDestinoId = especialidadesDelCentro.get(0).id;

            // 1. Insertar en tabla real de Asignaturas
            Asignatura nuevaReal = new Asignatura(sugerencia.nombreSugerido, sugerencia.cursoSugerido, especialidadDestinoId);
            db.asignaturaDao().insertar(nuevaReal);

            // 2. Borrar sugerencia
            db.sugerenciaMateriaDao().eliminarPorId(sugerencia.id);

            runOnUiThread(() -> {
                Toast.makeText(this, "Materia aprobada e integrada a la primera especialidad", Toast.LENGTH_LONG).show();
                cargarDatos();
            });
        });
    }

    private void rechazarMateria(SugerenciaMateria sugerencia) {
        executor.execute(() -> {
            db.sugerenciaMateriaDao().eliminarPorId(sugerencia.id);
            runOnUiThread(() -> {
                Toast.makeText(this, "Materia descartada", Toast.LENGTH_SHORT).show();
                cargarDatos();
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
