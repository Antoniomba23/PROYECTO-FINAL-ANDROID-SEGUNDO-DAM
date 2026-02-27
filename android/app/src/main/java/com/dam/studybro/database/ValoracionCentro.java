package com.dam.studybro.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.Index;
import androidx.room.Ignore;

@Entity(tableName = "valoraciones_centro",
        foreignKeys = {
            @ForeignKey(entity = Centro.class,
                        parentColumns = "id",
                        childColumns = "centro_id",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("usuario_id"), @Index("centro_id")})
public class ValoracionCentro {
    @PrimaryKey(autoGenerate = true)
    @com.google.gson.annotations.SerializedName("id")
    public int id;

    @ColumnInfo(name = "puntuacion")
    @com.google.gson.annotations.SerializedName("puntuacion")
    public int puntuacion; // 1 to 5

    @ColumnInfo(name = "comentario")
    @com.google.gson.annotations.SerializedName("comentario")
    public String comentario;

    @ColumnInfo(name = "fecha")
    @com.google.gson.annotations.SerializedName("fecha")
    public long fecha;

    @ColumnInfo(name = "usuario_id")
    @com.google.gson.annotations.SerializedName("usuario_id")
    public String usuarioEmail; // Cambiado de int a String para usar el email/uuid de Supabase

    @ColumnInfo(name = "centro_id")
    @com.google.gson.annotations.SerializedName("centro_id")
    public int centroId;

    public ValoracionCentro() {}

    @Ignore
    public ValoracionCentro(int puntuacion, String comentario, long fecha, String usuarioEmail, int centroId) {
        this.puntuacion = puntuacion;
        this.comentario = comentario;
        this.fecha = fecha;
        this.usuarioEmail = usuarioEmail;
        this.centroId = centroId;
    }
}
