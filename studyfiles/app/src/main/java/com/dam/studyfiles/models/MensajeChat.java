package com.dam.studyfiles.models;

import com.google.gson.annotations.SerializedName;

public class MensajeChat {
    @SerializedName("id")
    public Long id;

    @SerializedName("usuario_id")
    public String usuarioId;

    @SerializedName("rol")
    public String rol; // "user" o "model"

    @SerializedName("mensaje")
    public String mensaje;

    @SerializedName("fecha")
    public long fecha;

    public MensajeChat() {}

    public MensajeChat(String usuarioId, String rol, String mensaje, long fecha) {
        this.usuarioId = usuarioId;
        this.rol = rol;
        this.mensaje = mensaje;
        this.fecha = fecha;
    }
}
