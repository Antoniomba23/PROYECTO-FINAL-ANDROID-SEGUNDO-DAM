package com.dam.studybro.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.Ignore;

@Entity(tableName = "sugerencias_materia",
        foreignKeys = @ForeignKey(entity = Centro.class,
                                  parentColumns = "id",
                                  childColumns = "centro_id",
                                  onDelete = ForeignKey.CASCADE),
        indices = {@Index("centro_id")})
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

    @ColumnInfo(name = "especialidad_id")
    public int especialidadId;

    public SugerenciaMateria() {}

    @Ignore
    public SugerenciaMateria(String nombre, int curso, int centroId, String email, int especialidadId) {
        this.nombreSugerido = nombre;
        this.cursoSugerido = curso;
        this.centroId = centroId;
        this.emailSolicitante = email;
        this.especialidadId = especialidadId;
    }
}
