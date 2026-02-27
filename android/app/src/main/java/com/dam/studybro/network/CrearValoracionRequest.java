package com.dam.studybro.network;

import com.google.gson.annotations.SerializedName;

/**
 * DTO para la creación de valoraciones de centros.
 * Se omite 'id' para evitar error 400 en Supabase.
 */
public class CrearValoracionRequest {

    @SerializedName("puntuacion")
    public int puntuacion;

    @SerializedName("comentario")
    public String comentario;

    @SerializedName("fecha")
    public long fecha;

    @SerializedName("usuario_id")
    public String usuarioEmail;

    @SerializedName("centro_id")
    public int centroId;

    public CrearValoracionRequest() {}
}
