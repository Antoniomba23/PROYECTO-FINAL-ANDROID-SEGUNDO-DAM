package com.dam.studybro.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface CentroEspecialidadDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertar(CentroEspecialidad ce);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertarLista(List<CentroEspecialidad> lista);

    @Query("SELECT e.* FROM especialidades e " +
           "INNER JOIN centro_especialidades ce ON e.id = ce.especialidad_id " +
           "WHERE ce.centro_id = :centroId")
    List<Especialidad> obtenerPorCentro(int centroId);
}
