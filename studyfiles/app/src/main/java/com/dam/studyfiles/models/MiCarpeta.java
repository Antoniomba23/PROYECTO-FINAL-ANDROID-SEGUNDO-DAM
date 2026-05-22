package com.dam.studyfiles.models;

import com.google.gson.annotations.SerializedName;

public class MiCarpeta {

    @SerializedName("id")
    public int id;

    @SerializedName("nombre")
    public String nombre;

    @SerializedName("usuario_id")
    public String usuarioId;

    @SerializedName("carpeta_padre_id")
    public Integer carpetaPadreId;

    @SerializedName("color")
    public String color;

    @SerializedName("fecha_creacion")
    public long fechaCreacion;
}
