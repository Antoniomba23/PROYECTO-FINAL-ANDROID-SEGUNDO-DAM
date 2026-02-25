package com.dam.studybro.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CentroDao {

    @Insert
    void insertar(Centro centro);

    /**
     * Inserción masiva — Room agrupa todos los inserts en una sola transacción
     * automáticamente cuando se pasa una List/Array, mucho más rápido que N inserts.
     */
    @Insert
    void insertarLista(List<Centro> centros);

    @Query("SELECT * FROM centros")
    List<Centro> obtenerTodos();

    @Query("SELECT * FROM centros WHERE id = :id LIMIT 1")
    Centro obtenerPorId(int id);

    @Query("SELECT codigo_api FROM centros")
    List<String> obtenerTodosLosCodigosApi();

    @Update
    void actualizar(Centro centro);
}
