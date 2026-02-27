package com.dam.studybro.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import java.util.List;

@Dao
public interface SugerenciaMateriaDao {
    @Query("SELECT * FROM sugerencias_materia")
    List<SugerenciaMateria> obtenerTodas();

    @Insert
    long insertar(SugerenciaMateria sugerencia);

    @Delete
    void eliminar(SugerenciaMateria sugerencia);

    @Query("DELETE FROM sugerencias_materia WHERE id = :sugerenciaId")
    void eliminarPorId(int sugerenciaId);
}
