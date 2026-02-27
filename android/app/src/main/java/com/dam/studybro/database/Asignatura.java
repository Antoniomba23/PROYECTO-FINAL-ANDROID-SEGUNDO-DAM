package com.dam.studybro.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.Ignore;

@Entity(tableName = "asignaturas",
        foreignKeys = @ForeignKey(entity = Especialidad.class,
                                  parentColumns = "id",
                                  childColumns = "especialidad_id",
                                  onDelete = ForeignKey.CASCADE),
        indices = {@Index("especialidad_id")})
public class Asignatura {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "nombre")
    public String nombre;

    @ColumnInfo(name = "curso")
    public int curso;

    @ColumnInfo(name = "especialidad_id")
    public int especialidadId;

    public Asignatura() {}

    @Ignore
    public Asignatura(String nombre, int curso, int especialidadId) {
        this.nombre = nombre;
        this.curso = curso;
        this.especialidadId = especialidadId;
    }
}
