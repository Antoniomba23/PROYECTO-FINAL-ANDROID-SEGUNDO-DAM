package com.dam.studybro.database;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Clase auxiliar para rellenar la Base de Datos con datos iniciales
 * si está vacía (Seed). Útil para Asignaturas y Especialidades.
 */
public class DatabaseSeeder {

    public static void sembrarDatos(BaseDatosApp db) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // Verificar si hay asignaturas
            int cantidad = db.asignaturaDao().contar();
            
            // Si está vacía, rellenamos
            if (cantidad == 0) {
                // 1. Crear Especialidades Básicas
                Especialidad dam = new Especialidad("Desarrollo de Aplicaciones Multiplataforma (DAM)", "DAM");
                Especialidad daw = new Especialidad("Desarrollo de Aplicaciones Web (DAW)", "DAW");
                
                long idDam = db.especialidadDao().insertar(dam);
                long idDaw = db.especialidadDao().insertar(daw);
                
                // 2. Crear Asignaturas para DAM (ID cast a int porque Room devuelve long)
                db.asignaturaDao().insertar(new Asignatura("programación", 1, (int)idDam));
                db.asignaturaDao().insertar(new Asignatura("Sistemas Informáticos", 1, (int)idDam));
                db.asignaturaDao().insertar(new Asignatura("Bases de Datos", 1, (int)idDam));
                db.asignaturaDao().insertar(new Asignatura("Entornos de Desarrollo", 1, (int)idDam));
                db.asignaturaDao().insertar(new Asignatura("Lenguajes de Marcas", 1, (int)idDam));
                
                db.asignaturaDao().insertar(new Asignatura("Acceso a Datos", 2, (int)idDam));
                db.asignaturaDao().insertar(new Asignatura("Desarrollo de Interfaces", 2, (int)idDam));
                db.asignaturaDao().insertar(new Asignatura("PMDM (Móviles)", 2, (int)idDam));
                db.asignaturaDao().insertar(new Asignatura("PSP (Servicios y Procesos)", 2, (int)idDam));
                db.asignaturaDao().insertar(new Asignatura("SGE (Empresarial)", 2, (int)idDam));

                // 3. Crear Asignaturas para DAW
                db.asignaturaDao().insertar(new Asignatura("Desarrollo Web Entorno Cliente", 2, (int)idDaw));
                db.asignaturaDao().insertar(new Asignatura("Desarrollo Web Entorno Servidor", 2, (int)idDaw));
                db.asignaturaDao().insertar(new Asignatura("Despliegue de Aplicaciones", 2, (int)idDaw));
            }
        });
    }
}
