package com.dam.studybro.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "valoraciones_centro",
        foreignKeys = {
            @ForeignKey(entity = Usuario.class,
                        parentColumns = "id",
                        childColumns = "usuario_id",
                        onDelete = ForeignKey.CASCADE),
            @ForeignKey(entity = Centro.class,
                        parentColumns = "id",
                        childColumns = "centro_id",
                        onDelete = ForeignKey.CASCADE)
        })
public class ValoracionCentro {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "puntuacion")
    public int puntuacion; // 1 to 5

    @ColumnInfo(name = "comentario")
    public String comentario;

    @ColumnInfo(name = "fecha")
    public long fecha;

    @ColumnInfo(name = "usuario_id")
    public int usuarioId;

    @ColumnInfo(name = "centro_id")
    public int centroId;

    public ValoracionCentro() {}

    public ValoracionCentro(int puntuacion, String comentario, long fecha, int usuarioId, int centroId) {
        this.puntuacion = puntuacion;
        this.comentario = comentario;
        this.fecha = fecha;
        this.usuarioId = usuarioId;
        this.centroId = centroId;
    }
}
