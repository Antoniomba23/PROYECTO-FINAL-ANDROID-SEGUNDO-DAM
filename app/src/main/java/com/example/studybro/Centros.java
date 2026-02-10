package com.example.studybro;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Centros {

    @SerializedName("items")
    List<Personaje> DragonBall   = new ArrayList<>();
    public  static class Personaje {

        @SerializedName("name")
        public String nombrePersonaje;
        @SerializedName("maxKi")
        public String maxKi;
        @SerializedName("description")
        public String description;
        @SerializedName("image")
        public String imagenUrl;
        @SerializedName("id")
        public String id;




    }

}

