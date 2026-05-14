package com.dam.studyfiles.network;

import com.dam.studyfiles.models.Usuario;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface UsuariosApi {

    /** Busca usuario por nombre (para login y verificar disponibilidad) */
    @GET("rest/v1/usuarios?select=id,nombre_usuario,contrasena")
    Call<List<Usuario>> buscarPorNombre(@Query("nombre_usuario") String filtro);

    /** Registrar nuevo usuario */
    @Headers("Prefer: return=representation")
    @POST("rest/v1/usuarios")
    Call<List<Usuario>> registrar(@Body Map<String, Object> body);
}
