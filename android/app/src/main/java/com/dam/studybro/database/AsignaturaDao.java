package com.dam.studybro.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface AsignaturaDao {
    @Insert
    void insertar(Asignatura asignatura);

    @Query("SELECT * FROM asignaturas")
    List<Asignatura> obtenerTodas();

    @Query("SELECT * FROM asignaturas WHERE especialidad_id = :especialidadId")
    List<Asignatura> obtenerPorEspecialidad(int especialidadId);
}
