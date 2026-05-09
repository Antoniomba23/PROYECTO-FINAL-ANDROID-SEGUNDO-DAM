package com.dam.studyfiles.models;

import com.google.gson.annotations.SerializedName;

/** Payload para INSERT en la tabla archivos de Supabase. */
public class SubirArchivoRequest {

    @SerializedName("nombre")
    public String nombre;

    @SerializedName("descripcion")
    public String descripcion;

    @SerializedName("categoria")
    public String categoria;

    @SerializedName("uploader")
    public String uploader;

    @SerializedName("url_archivo")
    public String urlArchivo;

    @SerializedName("tipo_archivo")
    public String tipoArchivo;

    @SerializedName("likes")
    public int likes = 0;

    @SerializedName("dislikes")
    public int dislikes = 0;

    @SerializedName("reportes")
    public int reportes = 0;

    @SerializedName("fecha_subida")
    public long fechaSubida = System.currentTimeMillis();
}
