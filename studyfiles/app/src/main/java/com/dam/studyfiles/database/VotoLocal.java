package com.dam.studyfiles.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Registro local del voto del usuario (like o dislike) por archivo.
 * Evita que el mismo dispositivo vote dos veces en el mismo archivo.
 */
@Entity(tableName = "votos_locales")
public class VotoLocal {

    @PrimaryKey
    public int archivoId;

    public String tipo; // "like" o "dislike"

    public VotoLocal() {}

    public VotoLocal(int archivoId, String tipo) {
        this.archivoId = archivoId;
        this.tipo = tipo;
    }
}
