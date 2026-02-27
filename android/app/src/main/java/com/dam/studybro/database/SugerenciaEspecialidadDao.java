package com.dam.studybro.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import java.util.List;

@Dao
public interface SugerenciaEspecialidadDao {
    @Query("SELECT * FROM sugerencias_especialidad")
    List<SugerenciaEspecialidad> obtenerTodas();

    @Insert
    long insertar(SugerenciaEspecialidad sugerencia);

    @Delete
    void eliminar(SugerenciaEspecialidad sugerencia);
    
    @Query("DELETE FROM sugerencias_especialidad WHERE id = :sugerenciaId")
    void eliminarPorId(int sugerenciaId);
}
