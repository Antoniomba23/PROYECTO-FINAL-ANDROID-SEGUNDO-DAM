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

    @androidx.room.Update
    void actualizar(Comentario comentario);

    @Query("DELETE FROM comentarios WHERE id = :id")
    void eliminar(int id);

    @Query("SELECT COUNT(*) FROM comentarios WHERE parent_id = :comentarioId")
    int contarRespuestas(int comentarioId);
}
