package com.dam.studybro.modelos;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RespuestaDatosMadrid {
    @SerializedName("@graph")
    public List<CentroMadrid> graph;
}
