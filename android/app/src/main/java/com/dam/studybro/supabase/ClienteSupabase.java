package com.dam.studybro.supabase;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Cliente Supabase - Punto central de conexión a la API REST de Supabase.
 * Usamos Retrofit  para las peticiones HTTP.
 */
public class ClienteSupabase {

    // ===================== CONFIGURACIÓN =====================
    public static final String URL_BASE = "https://flpdwxgobctdkudovdyx.supabase.co/";
    public static final String API_KEY  = "sb_publishable_UIlhHgMKteF9cS0Th7ztLA_9qT7VnTX";
    // =========================================================

    private static Retrofit retrofit = null;

    /** Instancia única de Retrofit apuntando a Supabase */
    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(URL_BASE)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    /** Servicio de Autenticación */
    public static ServicioAuth getAuth() {
        return getRetrofit().create(ServicioAuth.class);
    }

    /** Servicio de Storage */
    public static ServicioStorage getStorage() {
        return getRetrofit().create(ServicioStorage.class);
    }
}
