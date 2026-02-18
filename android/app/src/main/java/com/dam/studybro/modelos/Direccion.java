package com.dam.studybro.modelos;

import com.google.gson.annotations.SerializedName;

public class Direccion {
    @SerializedName("locality")
    public String locality;

    @SerializedName("postal-code")
    public String postalCode;

    @SerializedName("street-address")
    public String streetAddress;
}
