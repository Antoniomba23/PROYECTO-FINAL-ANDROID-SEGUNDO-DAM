package com.dam.studybro.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.Ignore;

@Entity(tableName = "centros")
public class Centro {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "nombre")
    public String nombre;

    @ColumnInfo(name = "ciudad")
    public String ciudad;

    @ColumnInfo(name = "direccion")
    public String direccion;

    @ColumnInfo(name = "web_url")
    public String webUrl;

    @ColumnInfo(name = "valoracion_media")
    public float valoracionMedia;

    @ColumnInfo(name = "codigo_api")
    public String codigoApi;

    @ColumnInfo(name = "imagen_url")
    public String imagenUrl;

    @ColumnInfo(name = "descripcion")
    public String descripcion;

    @ColumnInfo(name = "horario")
    public String horario;

    @ColumnInfo(name = "accesibilidad")
    public String accesibilidad;

    @ColumnInfo(name = "servicios")
    public String servicios;

    @ColumnInfo(name = "tipo")
    public String tipo;

    @ColumnInfo(name = "url_detalle")
    public String urlDetalle;


    // Constructor vacío requerido por Room
    public Centro() {}

    @Ignore
    public Centro(String nombre, String ciudad, String direccion, String webUrl, float valoracionMedia, String codigoApi, String imagenUrl) {
        this.nombre = nombre;
        this.ciudad = ciudad;
        this.direccion = direccion;
        this.webUrl = webUrl;
        this.valoracionMedia = valoracionMedia;
        this.codigoApi = codigoApi;
        this.imagenUrl = imagenUrl;
    }
}
