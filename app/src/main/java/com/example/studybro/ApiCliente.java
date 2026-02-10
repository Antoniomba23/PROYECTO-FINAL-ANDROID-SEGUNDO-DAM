package com.example.studybro;


import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiCliente {


    public static String BASE_URL = "https://dragonball-api.com/api/";


    public static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Configuración del interceptor para ver los logs de las peticiones
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Configuración del cliente OkHttp
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .build();

            // Configuración de Retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create()) // Se agrega el conversor (usualmente Gson)
                    .client(client)
                    .build();
        }
        return retrofit;
    }
}


