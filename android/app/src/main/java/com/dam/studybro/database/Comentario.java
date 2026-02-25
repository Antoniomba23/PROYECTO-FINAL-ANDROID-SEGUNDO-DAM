package com.dam.studybro.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

/**
 * Comentario de una publicación.
 * usuarioId es el email/UUID de Supabase (String), no un FK local.
 */
@Entity(tableName = "comentarios",
        foreignKeys = {
            @ForeignKey(entity = Publicacion.class,
                        parentColumns = "id",
                        childColumns = "publicacion_id",
                        onDelete = ForeignKey.CASCADE)
        })
public class Comentario {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "contenido")
    public String contenido;

    @ColumnInfo(name = "fecha")
    public long fecha;

    /** Email del usuario de Supabase (no FK local) */
    @ColumnInfo(name = "usuario_id")
    public String usuarioId;

    @ColumnInfo(name = "publicacion_id")
    public int publicacionId;

    public Comentario() {}

    public Comentario(String contenido, long fecha, String usuarioId, int publicacionId) {
        this.contenido    = contenido;
        this.fecha        = fecha;
        this.usuarioId    = usuarioId;
        this.publicacionId = publicacionId;
    }
}
