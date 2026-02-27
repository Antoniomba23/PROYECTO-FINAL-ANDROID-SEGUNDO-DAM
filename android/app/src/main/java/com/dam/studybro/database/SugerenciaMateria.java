package com.dam.studybro.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;

@Entity(tableName = "sugerencias_materia",
        foreignKeys = @ForeignKey(entity = Centro.class,
                                  parentColumns = "id",
                                  childColumns = "centro_id",
                                  onDelete = ForeignKey.CASCADE))
public class SugerenciaMateria {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "nombre_sugerido")
    public String nombreSugerido;

    @ColumnInfo(name = "centro_id")
    public int centroId;

    @ColumnInfo(name = "email_solicitante")
    public String emailSolicitante; 
    
    @ColumnInfo(name = "curso_sugerido")
    public int cursoSugerido;

    public SugerenciaMateria() {}

    public SugerenciaMateria(String nombre, int curso, int centroId, String email) {
        this.nombreSugerido = nombre;
        this.cursoSugerido = curso;
        this.centroId = centroId;
        this.emailSolicitante = email;
    }
}
