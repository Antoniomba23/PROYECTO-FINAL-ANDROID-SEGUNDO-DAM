package com.dam.studybro.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ComentarioDao {
    @Insert
    void insertar(Comentario comentario);

    @Query("SELECT * FROM comentarios WHERE publicacion_id = :publicacionId ORDER BY fecha DESC")
    List<Comentario> obtenerPorPublicacion(int publicacionId);
}
