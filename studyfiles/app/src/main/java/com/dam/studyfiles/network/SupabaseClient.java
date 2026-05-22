package com.dam.studyfiles.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SupabaseClient {

    public static final String URL_BASE = "https://flpdwxgobctdkudovdyx.supabase.co/";
    public static final String API_KEY  = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZscGR3eGdvYmN0ZGt1ZG92ZHl4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzE3MzY4MDAsImV4cCI6MjA4NzMxMjgwMH0.2W-qJiyxeBNOLRXyC6kMtwJ5myX_TXRomns-jhuIJ0c";

    private static Retrofit retrofit;

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(chain -> chain.proceed(
                            chain.request().newBuilder()
                                    .addHeader("apikey", API_KEY)
                                    .addHeader("Authorization", "Bearer " + API_KEY)
                                    .addHeader("Content-Type", "application/json")
                                    .addHeader("Prefer", "return=minimal")
                                    .build()
                    ))
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(URL_BASE)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static SupabaseApi getApi() {
        return getRetrofit().create(SupabaseApi.class);
    }

    public static MiNubeApi getMiNube() {
        return getRetrofit().create(MiNubeApi.class);
    }

    public static UsuariosApi getUsuarios() {
        return getRetrofit().create(UsuariosApi.class);
    }

    public static ChatApi getChat() {
        return getRetrofit().create(ChatApi.class);
    }
}
