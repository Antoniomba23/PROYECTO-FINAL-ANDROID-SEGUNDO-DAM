package com.dam.studybro.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;import androidx.room.Index;
import androidx.room.Ignore;

/**
 * Comentario de una publicación.
 * usuarioId es el email/UUID de Supabase (String), no un FK local.
 */
@Entity(tableName = "comentarios",
        indices = {@Index("publicacion_id")})
public class Comentario {
    @PrimaryKey(autoGenerate = true)
    @com.google.gson.annotations.SerializedName("id")
    public int id;

    @ColumnInfo(name = "contenido")
    @com.google.gson.annotations.SerializedName("contenido")
    public String contenido;

    @ColumnInfo(name = "fecha")
    @com.google.gson.annotations.SerializedName("fecha")
    public long fecha;

    /** Email del usuario de Supabase (no FK local) */
    @ColumnInfo(name = "usuario_id")
    @com.google.gson.annotations.SerializedName("usuario_id")
    public String usuarioId;

    @ColumnInfo(name = "publicacion_id")
    @com.google.gson.annotations.SerializedName("publicacion_id")
    public int publicacionId;

    /** ID del comentario al que se responde (null si es raíz) */
    @ColumnInfo(name = "parent_id")
    @com.google.gson.annotations.SerializedName("parent_id")
    public Integer parentId;

    public Comentario() {}

    @Ignore
    public Comentario(String contenido, long fecha, String usuarioId, int publicacionId) {
        this.contenido    = contenido;
        this.fecha        = fecha;
        this.usuarioId    = usuarioId;
        this.publicacionId = publicacionId;
    }
}
