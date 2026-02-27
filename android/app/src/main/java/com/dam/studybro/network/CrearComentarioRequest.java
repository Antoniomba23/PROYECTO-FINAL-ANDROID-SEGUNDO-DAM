package com.dam.studybro.network;

import com.google.gson.annotations.SerializedName;

/**
 * DTO para la creación de comentarios.
 * Se omite 'id' para evitar error 400 en Supabase.
 */
public class CrearComentarioRequest {
    
    @SerializedName("contenido")
    public String contenido;

    @SerializedName("fecha")
    public long fecha;

    @SerializedName("usuario_id")
    public String usuarioId;

    @SerializedName("publicacion_id")
    public int publicacionId;

    @SerializedName("parent_id")
    public Integer parentId;

    public CrearComentarioRequest() {}
}
