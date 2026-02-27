package com.dam.studybro.network;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SupabaseClient {

    private static final String BASE_URL = "https://flpdwxgobctdkudovdyx.supabase.co/rest/v1/";
    // NOTA: Usualmente se usa la ANON KEY de Supabase en clientes REST públicos
    // Puesto que el usuario me ha dado la SECRET KEY (Service Role), la usaré bajo su responsabilidad para acceso total.
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZscGR3eGdvYmN0ZGt1ZG92ZHl4Iiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc3MTczNjgwMCwiZXhwIjoyMDg3MzEyODAwfQ.Rzowq5BIn0Fy-kUPaBl2XQYSOMW3N1mECWaPbMmoD98";
    
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {

            // Interceptor para inyectar automáticamente la API Key y Bearer Token
            Interceptor authInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("apikey", API_KEY)
                            .header("Authorization", "Bearer " + API_KEY)
                            .header("Content-Type", "application/json");

                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
