package com.example.studybro;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterfaz {

    @GET("egob/catalogo/300614-0-centros-educativos.json") // endpoint de DRagonBall Url
    Call<Centros> getCentros(

    );
}

