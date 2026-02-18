package com.dam.studybro.modelos;

import com.google.gson.annotations.SerializedName;

public class Organizacion {
    @SerializedName("organization-desc")
    public String organizationDesc;

    @SerializedName("accesibility")
    public String accesibility;

    @SerializedName("schedule")
    public String schedule;

    @SerializedName("services")
    public String services;
}
