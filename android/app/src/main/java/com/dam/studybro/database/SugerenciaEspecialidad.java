package com.dam.studybro.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.Ignore;

@Entity(tableName = "sugerencias_especialidad",
        foreignKeys = @ForeignKey(entity = Centro.class,
                                  parentColumns = "id",
                                  childColumns = "centro_id",
                                  onDelete = ForeignKey.CASCADE),
        indices = {@Index("centro_id")})
public class SugerenciaEspecialidad {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "nombre_sugerido")
    public String nombreSugerido;

    @ColumnInfo(name = "centro_id")
    public int centroId;

    @ColumnInfo(name = "email_solicitante")
    public String emailSolicitante; // Para saber quién la pidió y mostrarla en admin

    public SugerenciaEspecialidad() {}

    @Ignore
    public SugerenciaEspecialidad(String nombre, int centroId, String email) {
        this.nombreSugerido = nombre;
        this.centroId = centroId;
        this.emailSolicitante = email;
    }
}
