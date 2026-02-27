package com.dam.studybro.database;

import androidx.room.Embedded;
import androidx.room.ColumnInfo;

public class SugerenciaEspecialidadConCentro {
    @Embedded
    public SugerenciaEspecialidad sugerencia;

    @ColumnInfo(name = "nombre_centro")
    public String nombreCentro;
}
