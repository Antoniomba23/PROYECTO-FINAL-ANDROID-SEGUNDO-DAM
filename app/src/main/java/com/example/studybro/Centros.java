
package com.example.studybro;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Centros {

    @SerializedName("@graph")
    public List<CentroMadrid> centrosMadrid;

    public static class CentroMadrid {
        @SerializedName("title")
        public String nombreCentro;

        @SerializedName("address")
        public Address address;

        @SerializedName("organization")
        public Organization organization;
    }

    public static class Address {
        // En el JSON es "street-address"
        @SerializedName("street-address")
        public String streetName;
    }

    public static class Organization {
        @SerializedName("organization-name")
        public String organizationName;

        @SerializedName("organization-desc")
        public String organizationDesc;
    }
}