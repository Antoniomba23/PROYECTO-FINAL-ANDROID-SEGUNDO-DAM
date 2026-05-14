package com.dam.studyfiles.network;

import com.dam.studyfiles.models.MensajeChat;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ChatApi {

    @Headers({
        "apikey: " + SupabaseClient.API_KEY,
        "Authorization: Bearer " + SupabaseClient.API_KEY
    })
    @GET("rest/v1/mensajes_chat?select=*&order=fecha.asc")
    Call<List<MensajeChat>> getHistorialChat(@Query("usuario_id") String eqUsuarioId);

    @Headers({
        "apikey: " + SupabaseClient.API_KEY,
        "Authorization: Bearer " + SupabaseClient.API_KEY,
        "Content-Type: application/json",
        "Prefer: return=representation"
    })
    @POST("rest/v1/mensajes_chat")
    Call<List<MensajeChat>> insertarMensaje(@Body MensajeChat mensaje);
}
