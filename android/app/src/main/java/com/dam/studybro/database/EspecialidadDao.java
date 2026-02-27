package com.dam.studybro.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface EspecialidadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertar(Especialidad especialidad);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertarLista(List<Especialidad> lista);

    @Query("SELECT * FROM especialidades")
    List<Especialidad> obtenerTodas();
}
