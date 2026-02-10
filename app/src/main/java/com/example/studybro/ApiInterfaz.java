package com.example.studybro;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterfaz {

    @GET("characters") // endpoint de DRagonBall Url
    Call<Centros> getPersonajes(
            @Query("page") int page,// Query -> page definie el parametro de la url
            @Query("limit") int limit //Quiery -> limit definie el parametro de la url


    );
}

