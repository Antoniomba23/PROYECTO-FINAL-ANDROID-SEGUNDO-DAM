package com.dam.studybro.supabase;

import com.google.gson.annotations.SerializedName;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Interfaz Retrofit para la API de Autenticación de Supabase.
 * Endpoints: /auth/v1/signup  y  /auth/v1/token
 */
public interface ServicioAuth {

    // ── REGISTRO ───────────────────────────────────────────────────────────
    @Headers({
        "Content-Type: application/json",
        "apikey: " + ClienteSupabase.API_KEY
    })
    @POST("auth/v1/signup")
    Call<RespuestaAuth> registrar(@Body PeticionAuth cuerpo);

    // ── LOGIN ──────────────────────────────────────────────────────────────
    @Headers({
        "Content-Type: application/json",
        "apikey: " + ClienteSupabase.API_KEY
    })
    @POST("auth/v1/token?grant_type=password")
    Call<RespuestaAuth> login(@Body PeticionAuth cuerpo);

    // ══════════════════ MODELOS ════════════════════════════════════════════

    /** Cuerpo de la petición (email + password) */
    class PeticionAuth {
        public String email;
        public String password;

        public PeticionAuth(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    /** Respuesta de Supabase con el token de sesión */
    class RespuestaAuth {
        @SerializedName("access_token")
        public String accessToken;

        @SerializedName("user")
        public UsuarioAuth usuario;

        // En signup, el token puede venir dentro de session
        @SerializedName("session")
        public SesionAuth sesion;
    }

    class SesionAuth {
        @SerializedName("access_token")
        public String accessToken;
    }

    class UsuarioAuth {
        public String id;
        public String email;
    }
}
