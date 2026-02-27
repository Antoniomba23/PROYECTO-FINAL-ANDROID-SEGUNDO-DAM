package com.dam.studybro.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CentroDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertar(Centro centro);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertarLista(List<Centro> centros);

    @Query("SELECT * FROM centros")
    List<Centro> obtenerTodos();

    @Query("SELECT * FROM centros WHERE id = :id LIMIT 1")
    Centro obtenerPorId(int id);

    @Query("SELECT codigo_api FROM centros")
    List<String> obtenerTodosLosCodigosApi();

    @Query("SELECT * FROM centros WHERE codigo_api IN (:codigos)")
    List<Centro> obtenerTodosPorCodigosApi(List<String> codigos);

    @Update
    void actualizar(Centro centro);
}
