package com.dam.studybro.supabase;

import com.google.gson.annotations.SerializedName;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

/**
 * Interfaz Retrofit para subir archivos a Supabase Storage.
 * Bucket usado: "publicaciones"
 */
public interface ServicioStorage {

    String BUCKET = "publicaciones";

    /**
     * Sube un archivo al bucket "publicaciones".
     * La ruta del archivo dentro del bucket es: {ruta}
     * Requiere el token de sesión del usuario logueado.
     */
    @Multipart
    @POST("storage/v1/object/{bucket}/{ruta}")
    Call<RespuestaStorage> subirArchivo(
            @Header("Authorization")  String bearerToken,     // "Bearer <access_token>"
            @Header("apikey")         String apiKey,
            @Path("bucket")           String bucket,
            @Path(value = "ruta", encoded = true) String ruta,
            @Part MultipartBody.Part  archivo
    );

    /** URL pública de un archivo ya subido */
    static String obtenerUrlPublica(String ruta) {
        return ClienteSupabase.URL_BASE
                + "storage/v1/object/public/"
                + BUCKET + "/" + ruta;
    }

    class RespuestaStorage {
        @SerializedName("Key")
        public String clave;
    }
}
