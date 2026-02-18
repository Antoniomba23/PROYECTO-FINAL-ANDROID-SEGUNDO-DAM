package com.dam.studybro.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface EspecialidadDao {
    @Insert
    long insertar(Especialidad especialidad);

    @Query("SELECT * FROM especialidades")
    List<Especialidad> obtenerTodas();
}
