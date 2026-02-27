package com.dam.studybro.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ValoracionCentroDao {

    @Insert
    void insertar(ValoracionCentro valoracion);

    @Query("SELECT * FROM valoraciones_centro WHERE centro_id = :centroId")
    List<ValoracionCentro> obtenerPorCentro(int centroId);

    // Calcular media (útil para verificar)
    @Query("SELECT AVG(puntuacion) FROM valoraciones_centro WHERE centro_id = :centroId")
    float obtenerMedia(int centroId);

    @Query("SELECT * FROM valoraciones_centro WHERE centro_id = :centroId AND usuario_id = :usuarioId LIMIT 1")
    ValoracionCentro obtenerPorUsuarioYCentro(String usuarioId, int centroId);

    @androidx.room.Update
    void actualizar(ValoracionCentro valoracion);

    @androidx.room.Delete
    void borrar(ValoracionCentro valoracion);
}
