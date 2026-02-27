package com.dam.studybro.network;

import com.google.gson.annotations.SerializedName;

/**
 * DTO para la creación de publicaciones.
 * Se omite el campo 'id' para que la base de datos Supabase asigne uno automáticamente 
 * y no rechace la inserción (HTTP 400/404) al recibir un id inválido como 0.
 */
public class CrearPublicacionRequest {
    
    @SerializedName("titulo")
    public String titulo;

    @SerializedName("descripcion")
    public String descripcion;

    @SerializedName("archivo_url")
    public String archivoUrl;

    @SerializedName("tipo")
    public String tipo;

    @SerializedName("anio_escolar")
    public String anioEscolar;

    @SerializedName("fecha_subida")
    public long fechaSubida;

    @SerializedName("curso")
    public String curso;

    @SerializedName("usuario_id")
    public String usuarioId;

    @SerializedName("asignatura_id")
    public int asignaturaId;

    public CrearPublicacionRequest() {}
}
