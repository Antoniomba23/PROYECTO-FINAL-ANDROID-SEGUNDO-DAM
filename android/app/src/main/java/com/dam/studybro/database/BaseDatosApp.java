package com.dam.studybro.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

/**
 * Nivel 4: Room - La Conexión Única (Database)
 */
@Database(entities = {
        Usuario.class,
        Centro.class,
        Especialidad.class,
        Asignatura.class,
        Publicacion.class,
        Comentario.class,
        Interaccion.class,
        ValoracionCentro.class,
        CentroEspecialidad.class
}, version = 7)
public abstract class BaseDatosApp extends RoomDatabase {
    public abstract UsuarioDao usuarioDao();
    public abstract CentroDao centroDao();
    public abstract EspecialidadDao especialidadDao();
    public abstract AsignaturaDao asignaturaDao();
    public abstract PublicacionDao publicacionDao();
    public abstract ComentarioDao comentarioDao();
    public abstract InteraccionDao interaccionDao();
    public abstract ValoracionCentroDao valoracionCentroDao();
    public abstract CentroEspecialidadDao centroEspecialidadDao();

    // Singleton Pattern
    private static volatile BaseDatosApp INSTANCE;

    public static BaseDatosApp getInstance(android.content.Context context) {
        if (INSTANCE == null) {
            synchronized (BaseDatosApp.class) {
                if (INSTANCE == null) {
                    INSTANCE = androidx.room.Room.databaseBuilder(context.getApplicationContext(),
                                    BaseDatosApp.class, "studybro-db")
                            .fallbackToDestructiveMigration() // IMPORTANTE: Borra BD si cambia versión
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
