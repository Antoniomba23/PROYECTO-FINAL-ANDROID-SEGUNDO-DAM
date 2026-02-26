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
            // ─── 1. ESPECIALIDADES Y NIVELES ───
            long idInf   = db.especialidadDao().insertar(new Especialidad("Educación Infantil", "INF"));
            long idPri   = db.especialidadDao().insertar(new Especialidad("Educación Primaria", "PRI"));
            long idEso   = db.especialidadDao().insertar(new Especialidad("Secundaria (ESO)",   "ESO"));
            long idBacC  = db.especialidadDao().insertar(new Especialidad("Bachillerato de Ciencias", "BACC"));
            long idBacH  = db.especialidadDao().insertar(new Especialidad("Bachillerato de Humanidades y CCSS", "BACH"));
            long idBacA  = db.especialidadDao().insertar(new Especialidad("Bachillerato de Artes", "BACA"));
            long idSMR   = db.especialidadDao().insertar(new Especialidad("Grado Medio - Sistemas Microinformáticos (SMR)", "SMR"));
            long idDam   = db.especialidadDao().insertar(new Especialidad("Grado Superior - Multiplataforma (DAM)", "DAM"));
            long idDaw   = db.especialidadDao().insertar(new Especialidad("Grado Superior - Web (DAW)", "DAW"));
            long idAsir  = db.especialidadDao().insertar(new Especialidad("Grado Superior - Sistemas y Redes (ASIR)", "ASIR"));
            long idEoi   = db.especialidadDao().insertar(new Especialidad("Escuela Oficial de Idiomas (EOI)", "EOI"));

            // ─── 2. ASIGNATURAS ───
            // Infantil y Primaria
            db.asignaturaDao().insertar(new Asignatura("Conocimiento del Medio", 1, (int) idPri));
            db.asignaturaDao().insertar(new Asignatura("Lengua Castellana y Literatura", 1, (int) idPri));
            db.asignaturaDao().insertar(new Asignatura("Matemáticas", 1, (int) idPri));
            db.asignaturaDao().insertar(new Asignatura("Inglés", 1, (int) idPri));
            db.asignaturaDao().insertar(new Asignatura("Educación Física", 1, (int) idPri));

            // ESO
            db.asignaturaDao().insertar(new Asignatura("Matemáticas", 1, (int) idEso));
            db.asignaturaDao().insertar(new Asignatura("Lengua Castellana", 1, (int) idEso));
            db.asignaturaDao().insertar(new Asignatura("Biología y Geología", 1, (int) idEso));
            db.asignaturaDao().insertar(new Asignatura("Geografía e Historia", 1, (int) idEso));
            db.asignaturaDao().insertar(new Asignatura("Física y Química", 3, (int) idEso));
            db.asignaturaDao().insertar(new Asignatura("Tecnología", 2, (int) idEso));
            db.asignaturaDao().insertar(new Asignatura("Inglés", 1, (int) idEso));
            db.asignaturaDao().insertar(new Asignatura("Educación Plástica y Visual", 1, (int) idEso));

            // Bachillerato - Ciencias
            db.asignaturaDao().insertar(new Asignatura("Matemáticas I", 1, (int) idBacC));
            db.asignaturaDao().insertar(new Asignatura("Matemáticas II", 2, (int) idBacC));
            db.asignaturaDao().insertar(new Asignatura("Física y Química", 1, (int) idBacC));
            db.asignaturaDao().insertar(new Asignatura("Física", 2, (int) idBacC));
            db.asignaturaDao().insertar(new Asignatura("Química", 2, (int) idBacC));
            db.asignaturaDao().insertar(new Asignatura("Biología", 2, (int) idBacC));
            db.asignaturaDao().insertar(new Asignatura("Dibujo Técnico", 1, (int) idBacC));

            // Bachillerato - Humanidades
            db.asignaturaDao().insertar(new Asignatura("Latín", 1, (int) idBacH));
            db.asignaturaDao().insertar(new Asignatura("Griego", 1, (int) idBacH));
            db.asignaturaDao().insertar(new Asignatura("Historia de la Filosofía", 2, (int) idBacH));
            db.asignaturaDao().insertar(new Asignatura("Matemáticas Aplicadas", 1, (int) idBacH));
            db.asignaturaDao().insertar(new Asignatura("Economía de la Empresa", 2, (int) idBacH));

            // SMR (Grado Medio)
            db.asignaturaDao().insertar(new Asignatura("Montaje y Mantenimiento de Equipos", 1, (int) idSMR));
            db.asignaturaDao().insertar(new Asignatura("Sistemas Operativos Monopuesto", 1, (int) idSMR));
            db.asignaturaDao().insertar(new Asignatura("Aplicaciones Ofimáticas", 1, (int) idSMR));
            db.asignaturaDao().insertar(new Asignatura("Redes Locales", 1, (int) idSMR));
            db.asignaturaDao().insertar(new Asignatura("Sistemas Operativos en Red", 2, (int) idSMR));
            db.asignaturaDao().insertar(new Asignatura("Seguridad Informática", 2, (int) idSMR));

            // DAM (Grado Superior)
            db.asignaturaDao().insertar(new Asignatura("Sistemas Informáticos", 1, (int) idDam));
            db.asignaturaDao().insertar(new Asignatura("Bases de Datos", 1, (int) idDam));
            db.asignaturaDao().insertar(new Asignatura("Programación", 1, (int) idDam));
            db.asignaturaDao().insertar(new Asignatura("Lenguajes de Marcas", 1, (int) idDam));
            db.asignaturaDao().insertar(new Asignatura("Entornos de Desarrollo", 1, (int) idDam));
            db.asignaturaDao().insertar(new Asignatura("Acceso a Datos", 2, (int) idDam));
            db.asignaturaDao().insertar(new Asignatura("Desarrollo de Interfaces", 2, (int) idDam));
            db.asignaturaDao().insertar(new Asignatura("Programación Multimedia y Disp. Móviles", 2, (int) idDam));
            db.asignaturaDao().insertar(new Asignatura("Programación de Servicios y Procesos", 2, (int) idDam));
            db.asignaturaDao().insertar(new Asignatura("Sistemas de Gestión Empresarial", 2, (int) idDam));
            db.asignaturaDao().insertar(new Asignatura("Proyecto DAM", 2, (int) idDam));
            db.asignaturaDao().insertar(new Asignatura("Formación y Orientación Laboral (FOL)", 1, (int) idDam));
            db.asignaturaDao().insertar(new Asignatura("Empresa e Iniciativa Emprendedora (EIE)", 2, (int) idDam));

            // DAW (Grado Superior)
            // Primero es común con DAM
            db.asignaturaDao().insertar(new Asignatura("Sistemas Informáticos", 1, (int) idDaw));
            db.asignaturaDao().insertar(new Asignatura("Bases de Datos", 1, (int) idDaw));
            db.asignaturaDao().insertar(new Asignatura("Programación", 1, (int) idDaw));
            db.asignaturaDao().insertar(new Asignatura("Lenguajes de Marcas", 1, (int) idDaw));
            db.asignaturaDao().insertar(new Asignatura("Entornos de Desarrollo", 1, (int) idDaw));
            // Segundo DAW
            db.asignaturaDao().insertar(new Asignatura("Desarrollo Web en Entorno Cliente", 2, (int) idDaw));
            db.asignaturaDao().insertar(new Asignatura("Desarrollo Web en Entorno Servidor", 2, (int) idDaw));
            db.asignaturaDao().insertar(new Asignatura("Despliegue de Aplicaciones Web", 2, (int) idDaw));
            db.asignaturaDao().insertar(new Asignatura("Diseño de Interfaces Web", 2, (int) idDaw));
            db.asignaturaDao().insertar(new Asignatura("Proyecto DAW", 2, (int) idDaw));
            db.asignaturaDao().insertar(new Asignatura("Formación y Orientación Laboral (FOL)", 1, (int) idDaw));
            db.asignaturaDao().insertar(new Asignatura("Empresa e Iniciativa Emprendedora (EIE)", 2, (int) idDaw));

            // ASIR (Grado Superior)
            db.asignaturaDao().insertar(new Asignatura("Implantación de Sistemas Operativos", 1, (int) idAsir));
            db.asignaturaDao().insertar(new Asignatura("Planificación y Administración de Redes", 1, (int) idAsir));
            db.asignaturaDao().insertar(new Asignatura("Fundamentos de Hardware", 1, (int) idAsir));
            db.asignaturaDao().insertar(new Asignatura("Gestión de Bases de Datos", 1, (int) idAsir));
            db.asignaturaDao().insertar(new Asignatura("Lenguajes de Marcas y Sist. Gest. Inf.", 1, (int) idAsir));
            db.asignaturaDao().insertar(new Asignatura("Administración de Sistemas Operativos", 2, (int) idAsir));
            db.asignaturaDao().insertar(new Asignatura("Servicios de Red e Internet", 2, (int) idAsir));
            db.asignaturaDao().insertar(new Asignatura("Implantación de Aplicaciones Web", 2, (int) idAsir));
            db.asignaturaDao().insertar(new Asignatura("Administración de Sistemas Gestores BD", 2, (int) idAsir));
            db.asignaturaDao().insertar(new Asignatura("Seguridad y Alta Disponibilidad", 2, (int) idAsir));

            // Idiomas
            db.asignaturaDao().insertar(new Asignatura("Inglés A1-A2", 1, (int) idEoi));
            db.asignaturaDao().insertar(new Asignatura("Inglés B1-B2", 2, (int) idEoi));
            db.asignaturaDao().insertar(new Asignatura("Inglés C1-C2", 3, (int) idEoi));
            db.asignaturaDao().insertar(new Asignatura("Francés A1-A2", 1, (int) idEoi));
            db.asignaturaDao().insertar(new Asignatura("Alemán A1-A2", 1, (int) idEoi));
        }
    }
}
