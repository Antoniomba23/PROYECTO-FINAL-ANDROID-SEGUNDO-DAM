package com.dam.studyfiles.models;

import com.google.gson.annotations.SerializedName;

public class MiArchivo {

    @SerializedName("id")
    public int id;

    @SerializedName("nombre")
    public String nombre;

    @SerializedName("carpeta_id")
    public Integer carpetaId; // null = raíz

    @SerializedName("usuario_id")
    public String usuarioId;

    @SerializedName("url_archivo")
    public String urlArchivo;

    @SerializedName("tipo_archivo")
    public String tipoArchivo;

    @SerializedName("fecha_subida")
    public long fechaSubida;
}
