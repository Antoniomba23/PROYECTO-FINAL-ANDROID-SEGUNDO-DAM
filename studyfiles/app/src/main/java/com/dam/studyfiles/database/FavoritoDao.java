package com.dam.studyfiles.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FavoritoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertar(Favorito favorito);

    @Delete
    void eliminar(Favorito favorito);

    @Query("SELECT * FROM favoritos ORDER BY fechaGuardado DESC")
    List<Favorito> obtenerTodos();

    @Query("SELECT COUNT(*) FROM favoritos WHERE id = :archivoId")
    int esFavorito(int archivoId);

    @Query("DELETE FROM favoritos WHERE id = :archivoId")
    void eliminarPorId(int archivoId);
}
