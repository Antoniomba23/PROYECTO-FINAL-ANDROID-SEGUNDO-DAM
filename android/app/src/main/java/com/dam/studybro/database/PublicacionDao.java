package com.dam.studybro.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface PublicacionDao {
    @Insert
    void insertar(Publicacion publicacion);

    @Update
    void actualizar(Publicacion publicacion);

    @Delete
    void eliminar(Publicacion publicacion);

    @Query("SELECT * FROM publicaciones ORDER BY fecha_subida DESC")
    List<Publicacion> obtenerTodas();

    @Query("SELECT * FROM publicaciones WHERE asignatura_id = :asignaturaId ORDER BY fecha_subida DESC")
    List<Publicacion> obtenerPorAsignatura(int asignaturaId);

    @Query("SELECT * FROM publicaciones WHERE id = :id LIMIT 1")
    Publicacion obtenerPorId(int id);

    @Query("SELECT * FROM publicaciones WHERE usuario_id = :usuarioId ORDER BY fecha_subida DESC")
    List<Publicacion> obtenerPorUsuario(String usuarioId);

    @Query("SELECT p.* FROM publicaciones p INNER JOIN interacciones i ON p.id = i.publicacion_id WHERE i.usuario_id = :usuarioId AND i.tipo = 'GUARDADO' ORDER BY p.fecha_subida DESC")
    List<Publicacion> obtenerFavoritos(String usuarioId);

    @Query("SELECT p.* FROM publicaciones p INNER JOIN interacciones i ON p.id = i.publicacion_id WHERE i.usuario_id = :usuarioId AND i.tipo = 'ME_GUSTA' ORDER BY p.fecha_subida DESC")
    List<Publicacion> obtenerUtiles(String usuarioId);
}
