package com.dam.studybro.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "comentarios",
        foreignKeys = {
            @ForeignKey(entity = Usuario.class,
                        parentColumns = "id",
                        childColumns = "usuario_id",
                        onDelete = ForeignKey.CASCADE),
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

    @ColumnInfo(name = "usuario_id")
    public int usuarioId;

    @ColumnInfo(name = "publicacion_id")
    public int publicacionId;

    public Comentario() {}

    public Comentario(String contenido, long fecha, int usuarioId, int publicacionId) {
        this.contenido = contenido;
        this.fecha = fecha;
        this.usuarioId = usuarioId;
        this.publicacionId = publicacionId;
    }
}
