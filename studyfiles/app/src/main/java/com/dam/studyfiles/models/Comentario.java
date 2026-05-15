package com.dam.studyfiles.models;

import com.google.gson.annotations.SerializedName;

public class Comentario {

    @SerializedName("id")
    public int id;

    @SerializedName("archivo_id")
    public int archivoId;

    @SerializedName("usuario_nombre")
    public String usuarioNombre;

    @SerializedName("texto")
    public String texto;

    @SerializedName("fecha")
    public String fecha;

    public Comentario() {}

    public Comentario(int archivoId, String usuarioNombre, String texto) {
        this.archivoId = archivoId;
        this.usuarioNombre = usuarioNombre;
        this.texto = texto;
    }
}
