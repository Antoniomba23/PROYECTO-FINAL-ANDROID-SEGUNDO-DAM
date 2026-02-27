package com.dam.studybro.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import java.util.List;

@Dao
public interface SugerenciaEspecialidadDao {
    @Query("SELECT s.*, c.nombre as nombre_centro FROM sugerencias_especialidad s " +
           "INNER JOIN centros c ON s.centro_id = c.id")
    List<SugerenciaEspecialidadConCentro> obtenerTodasConCentro();

    @Insert
    long insertar(SugerenciaEspecialidad sugerencia);

    @Delete
    void eliminar(SugerenciaEspecialidad sugerencia);
    
    @Query("DELETE FROM sugerencias_especialidad WHERE id = :sugerenciaId")
    void eliminarPorId(int sugerenciaId);
}
