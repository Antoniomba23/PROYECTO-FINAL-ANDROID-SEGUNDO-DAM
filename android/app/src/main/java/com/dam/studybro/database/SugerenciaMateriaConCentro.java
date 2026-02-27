package com.dam.studybro.database;

import androidx.room.Embedded;
import androidx.room.ColumnInfo;

public class SugerenciaMateriaConCentro {
    @Embedded
    public SugerenciaMateria sugerencia;

    @ColumnInfo(name = "nombre_centro")
    public String nombreCentro;

    @ColumnInfo(name = "nombre_especialidad")
    public String nombreEspecialidad;
}
