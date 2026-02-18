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
        ValoracionCentro.class
}, version = 1)
public abstract class BaseDatosApp extends RoomDatabase {
    public abstract UsuarioDao usuarioDao();
    public abstract CentroDao centroDao();
    public abstract EspecialidadDao especialidadDao();
    public abstract AsignaturaDao asignaturaDao();
    public abstract PublicacionDao publicacionDao();
    public abstract ComentarioDao comentarioDao();
    public abstract InteraccionDao interaccionDao();
    public abstract ValoracionCentroDao valoracionCentroDao();
}
