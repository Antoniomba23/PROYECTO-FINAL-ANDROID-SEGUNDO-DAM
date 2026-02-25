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
            // 1. Especialidades y Niveles Educativos
            Especialidad infantil = new Especialidad("Educación Infantil", "INF");
            Especialidad primaria = new Especialidad("Educación Primaria", "PRI");
            Especialidad eso      = new Especialidad("Secundaria (ESO)",   "ESO");
            Especialidad bachi    = new Especialidad("Bachillerato",        "BAC");
            Especialidad dam      = new Especialidad("DAM (Informática)",   "DAM");
            Especialidad daw      = new Especialidad("DAW (Informática)",   "DAW");

            long idInf  = db.especialidadDao().insertar(infantil);
            long idPri  = db.especialidadDao().insertar(primaria);
            long idEso  = db.especialidadDao().insertar(eso);
            long idBac  = db.especialidadDao().insertar(bachi);
            long idDam  = db.especialidadDao().insertar(dam);
            long idDaw  = db.especialidadDao().insertar(daw);

            // 2. Asignaturas ESO / Bachillerato
            db.asignaturaDao().insertar(new Asignatura("Matemáticas", 1, (int) idEso));
            db.asignaturaDao().insertar(new Asignatura("Lengua y Literatura", 1, (int) idEso));
            db.asignaturaDao().insertar(new Asignatura("Matemáticas I", 1, (int) idBac));
            db.asignaturaDao().insertar(new Asignatura("Física y Química", 1, (int) idBac));

            // 3. Asignaturas DAM
            db.asignaturaDao().insertar(new Asignatura("Programación",                   1, (int) idDam));
            db.asignaturaDao().insertar(new Asignatura("Bases de Datos",                  1, (int) idDam));
            db.asignaturaDao().insertar(new Asignatura("Acceso a Datos",                  2, (int) idDam));
            db.asignaturaDao().insertar(new Asignatura("PMDM (Móviles)",                  2, (int) idDam));
            db.asignaturaDao().insertar(new Asignatura("PSP (Servicios y Procesos)",      2, (int) idDam));

            // 4. Asignaturas DAW
            db.asignaturaDao().insertar(new Asignatura("Desarrollo Web Entorno Cliente",  2, (int) idDaw));
            db.asignaturaDao().insertar(new Asignatura("Desarrollo Web Entorno Servidor", 2, (int) idDaw));
        }
    }
}
