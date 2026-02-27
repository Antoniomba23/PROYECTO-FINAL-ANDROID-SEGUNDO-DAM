package com.dam.studybro.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface InteraccionDao {

    @Insert
    void insertar(Interaccion interaccion);

    @Delete
    void eliminar(Interaccion interaccion);

    /** Cuenta cuántos usuarios han dado un tipo de interacción a una publicación */
    @Query("SELECT COUNT(*) FROM interacciones WHERE publicacion_id = :publicacionId AND tipo = :tipo")
    int contarInteracciones(int publicacionId, String tipo);

    /** Obtiene los IDs de las publicaciones con las que un usuario ha interactuado */
    @Query("SELECT publicacion_id FROM interacciones WHERE usuario_id = :usuarioId AND tipo = :tipo")
    java.util.List<Integer> obtenerIdsInteracciones(String usuarioId, String tipo);

    /** Busca si el usuario ya interactuó con una publicación de cierto tipo */
    @Query("SELECT * FROM interacciones WHERE publicacion_id = :publicacionId AND usuario_id = :usuarioId AND tipo = :tipo LIMIT 1")
    Interaccion obtenerInteraccion(int publicacionId, String usuarioId, String tipo);

    /** Alias previo, mantenido por compatibilidad */
    @Query("SELECT * FROM interacciones WHERE usuario_id = :usuarioId AND publicacion_id = :publicacionId AND tipo = :tipo LIMIT 1")
    Interaccion obtenerPorUsuarioPublicacionTipo(String usuarioId, int publicacionId, String tipo);
}
