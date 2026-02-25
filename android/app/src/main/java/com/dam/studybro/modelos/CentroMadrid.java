package com.dam.studybro.modelos;

import com.google.gson.annotations.SerializedName;

public class CentroMadrid {
    @SerializedName("title")
    public String title;

    @SerializedName("id")
    public String id;

    @SerializedName("address")
    public Direccion address;

    @SerializedName("relation")
    public String relation;

    @SerializedName("organization")
    public Organizacion organization;

    @SerializedName("location")
    public Ubicacion location;
}
