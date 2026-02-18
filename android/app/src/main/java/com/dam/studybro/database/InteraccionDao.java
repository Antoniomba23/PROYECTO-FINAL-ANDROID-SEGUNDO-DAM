package com.dam.studybro.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface InteraccionDao {
    @Insert
    void insertar(Interaccion interaccion);

    @Query("SELECT COUNT(*) FROM interacciones WHERE publicacion_id = :publicacionId AND tipo = :tipo")
    int contarInteracciones(int publicacionId, String tipo);

    @Query("SELECT * FROM interacciones WHERE usuario_id = :usuarioId AND publicacion_id = :publicacionId LIMIT 1")
    Interaccion obtenerPorUsuarioYPublicacion(int usuarioId, int publicacionId);
}
