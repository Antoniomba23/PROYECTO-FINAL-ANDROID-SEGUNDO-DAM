package com.dam.studybro.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "interacciones",
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
public class Interaccion {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "tipo")
    public String tipo; // "ME_GUSTA", "GUARDADO"

    @ColumnInfo(name = "usuario_id")
    public int usuarioId;

    @ColumnInfo(name = "publicacion_id")
    public int publicacionId;

    public Interaccion() {}

    public Interaccion(String tipo, int usuarioId, int publicacionId) {
        this.tipo = tipo;
        this.usuarioId = usuarioId;
        this.publicacionId = publicacionId;
    }
}
