package com.dam.studybro.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface PublicacionDao {
    @Insert
    void insertar(Publicacion publicacion);

    @Query("SELECT * FROM publicaciones ORDER BY fecha_subida DESC")
    List<Publicacion> obtenerTodas();

    @Query("SELECT * FROM publicaciones WHERE asignatura_id = :asignaturaId ORDER BY fecha_subida DESC")
    List<Publicacion> obtenerPorAsignatura(int asignaturaId);

    @Query("SELECT * FROM publicaciones WHERE usuario_id = :usuarioId ORDER BY fecha_subida DESC")
    List<Publicacion> obtenerPorUsuario(int usuarioId);
}
