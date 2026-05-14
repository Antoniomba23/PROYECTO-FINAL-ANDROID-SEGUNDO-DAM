package com.dam.studyfiles.models;

import com.google.gson.annotations.SerializedName;

public class Usuario {

    @SerializedName("id")
    public int id;

    @SerializedName("nombre_usuario")
    public String nombreUsuario;

    @SerializedName("contrasena")
    public String contrasena; // SHA-256 hash

    @SerializedName("fecha_registro")
    public long fechaRegistro;
}
