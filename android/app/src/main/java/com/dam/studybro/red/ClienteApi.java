package com.dam.studybro.red;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ClienteApi {
    private static final String BASE_URL = "https://datos.madrid.es/egob/";
    private static Retrofit retrofit = null;

    public static ServicioApi obtenerInstancia() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ServicioApi.class);
    }
}
