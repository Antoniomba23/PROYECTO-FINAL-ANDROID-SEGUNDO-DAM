package com.dam.studyfiles.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface VotoLocalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertar(VotoLocal voto);

    @Query("SELECT tipo FROM votos_locales WHERE archivoId = :archivoId LIMIT 1")
    String obtenerVoto(int archivoId);

    @Query("DELETE FROM votos_locales WHERE archivoId = :archivoId")
    void eliminar(int archivoId);
}
