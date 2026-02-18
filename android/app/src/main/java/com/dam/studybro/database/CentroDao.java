package com.dam.studybro.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface CentroDao {
    @Insert
    void insertar(Centro centro);

    @Query("SELECT * FROM centros")
    List<Centro> obtenerTodos();

    @Query("SELECT * FROM centros WHERE id = :id LIMIT 1")
    Centro obtenerPorId(int id);

    @Query("SELECT * FROM centros WHERE codigo_api = :codigoApi LIMIT 1")
    Centro obtenerPorCodigoApi(String codigoApi);
}
