package com.dam.studybro.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import java.util.List;

@Dao
public interface SugerenciaMateriaDao {
    @Query("SELECT s.*, c.nombre as nombre_centro, e.nombre as nombre_especialidad " +
           "FROM sugerencias_materia s " +
           "INNER JOIN centros c ON s.centro_id = c.id " +
           "LEFT JOIN especialidades e ON s.especialidad_id = e.id")
    List<SugerenciaMateriaConCentro> obtenerTodasConCentro();

    @Insert
    long insertar(SugerenciaMateria sugerencia);

    @Delete
    void eliminar(SugerenciaMateria sugerencia);

    @Query("DELETE FROM sugerencias_materia WHERE id = :sugerenciaId")
    void eliminarPorId(int sugerenciaId);
}
