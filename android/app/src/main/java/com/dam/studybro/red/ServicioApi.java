package com.dam.studybro.red;

import com.dam.studybro.modelos.RespuestaDatosMadrid;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;

public interface ServicioApi {
    @Headers("Accept: application/json")
    @GET("catalogo/300614-0-centros-educativos.json")
    Call<RespuestaDatosMadrid> obtenerCentrosEducativos();
}
