package com.dam.studyfiles.models;

import com.google.gson.annotations.SerializedName;

public class Comentario {

    @SerializedName("id")
    public long id;

    @SerializedName("contenido")
    public String contenido;          // era "texto" — campo real en Supabase

    @SerializedName("fecha")
    public long fecha;                // bigint (epoch millis)

    @SerializedName("usuario_id")
    public String usuarioId;

    @SerializedName("usuario_nombre")
    public String usuarioNombre;      // Nueva columna

    @SerializedName("publicacion_id")
    public long publicacionId;        // era "archivo_id"

    @SerializedName("parent_id")
    public Long parentId;             // nullable

    public Comentario() {}

    public Comentario(long publicacionId, String usuarioId, String contenido) {
        this.publicacionId = publicacionId;
        this.usuarioId     = usuarioId;
        this.contenido     = contenido;
        this.fecha         = System.currentTimeMillis();
    }
}
