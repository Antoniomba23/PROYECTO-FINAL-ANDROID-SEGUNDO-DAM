package com.dam.studyfiles.models;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo que representa un archivo en la tabla `archivos` de Supabase.
 */
public class Archivo {

    @SerializedName("id")
    public int id;

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
    public int likes;

    @SerializedName("dislikes")
    public int dislikes;

    @SerializedName("reportes")
    public int reportes;

    @SerializedName("fecha_subida")
    public long fechaSubida;

    // Helper: ratio de likes sobre total de votos (0-1)
    public float getRatio() {
        int total = likes + dislikes;
        if (total == 0) return 0f;
        return (float) likes / total;
    }
}
