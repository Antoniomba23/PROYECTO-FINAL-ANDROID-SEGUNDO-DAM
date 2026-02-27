package com.dam.studybro.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface AsignaturaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertar(Asignatura asignatura);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertarLista(List<Asignatura> asignaturas);

    @Query("SELECT * FROM asignaturas")
    List<Asignatura> obtenerTodas();

    @Query("SELECT COUNT(*) FROM asignaturas")
    int contar();

    @Query("SELECT * FROM asignaturas WHERE especialidad_id = :especialidadId")
    List<Asignatura> obtenerPorEspecialidad(int especialidadId);

    @Query("SELECT * FROM asignaturas WHERE id = :id LIMIT 1")
    Asignatura obtenerPorId(int id);
}
