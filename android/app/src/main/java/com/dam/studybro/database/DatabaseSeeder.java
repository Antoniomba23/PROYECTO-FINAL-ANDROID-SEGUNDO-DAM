package com.dam.studybro.database;

/**
 * Rellena la BD con datos iniciales si está vacía.
 * IMPORTANTE: Siempre llamar desde un hilo de background (no MainThread).
 */
public class DatabaseSeeder {

    /** Ejecutar SIEMPRE desde un hilo secundario (ExecutorService, AsyncTask, etc.) */
    public static void sembrarDatos(BaseDatosApp db) {
        // Ya estamos en background (lo garantiza el llamador en ActividadPrincipal)
        int cantidad = db.asignaturaDao().contar();

        if (cantidad == 0) {
            // 1. Especialidades
            Especialidad dam = new Especialidad("Desarrollo de Aplicaciones Multiplataforma (DAM)", "DAM");
            Especialidad daw = new Especialidad("Desarrollo de Aplicaciones Web (DAW)", "DAW");

            long idDam = db.especialidadDao().insertar(dam);
            long idDaw = db.especialidadDao().insertar(daw);

            // 2. Asignaturas DAM
            db.asignaturaDao().insertar(new Asignatura("Programación",                   1, (int) idDam));
            db.asignaturaDao().insertar(new Asignatura("Sistemas Informáticos",           1, (int) idDam));
            db.asignaturaDao().insertar(new Asignatura("Bases de Datos",                  1, (int) idDam));
            db.asignaturaDao().insertar(new Asignatura("Entornos de Desarrollo",          1, (int) idDam));
            db.asignaturaDao().insertar(new Asignatura("Lenguajes de Marcas",             1, (int) idDam));
            db.asignaturaDao().insertar(new Asignatura("Acceso a Datos",                  2, (int) idDam));
            db.asignaturaDao().insertar(new Asignatura("Desarrollo de Interfaces",        2, (int) idDam));
            db.asignaturaDao().insertar(new Asignatura("PMDM (Móviles)",                  2, (int) idDam));
            db.asignaturaDao().insertar(new Asignatura("PSP (Servicios y Procesos)",      2, (int) idDam));
            db.asignaturaDao().insertar(new Asignatura("SGE (Empresarial)",               2, (int) idDam));

            // 3. Asignaturas DAW
            db.asignaturaDao().insertar(new Asignatura("Desarrollo Web Entorno Cliente",  2, (int) idDaw));
            db.asignaturaDao().insertar(new Asignatura("Desarrollo Web Entorno Servidor", 2, (int) idDaw));
            db.asignaturaDao().insertar(new Asignatura("Despliegue de Aplicaciones",      2, (int) idDaw));
        }
    }
}
