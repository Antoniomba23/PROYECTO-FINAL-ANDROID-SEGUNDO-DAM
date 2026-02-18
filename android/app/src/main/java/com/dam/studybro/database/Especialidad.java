package com.dam.studybro.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "especialidades")
public class Especialidad {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "nombre")
    public String nombre;

    @ColumnInfo(name = "codigo_api")
    public String codigoApi;

    public Especialidad() {}

    public Especialidad(String nombre, String codigoApi) {
        this.nombre = nombre;
        this.codigoApi = codigoApi;
    }
}
