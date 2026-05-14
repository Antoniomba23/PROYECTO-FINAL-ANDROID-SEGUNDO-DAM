package com.dam.studyfiles.network;

import com.dam.studyfiles.models.MiArchivo;
import com.dam.studyfiles.models.MiCarpeta;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface MiNubeApi {

    // ── CARPETAS ─────────────────────────────────────────────────────────────

    @GET("rest/v1/mis_carpetas?order=fecha_creacion.desc")
    Call<List<MiCarpeta>> getMisCarpetas(@Query("usuario_id") String usuarioIdFiltro);

    @Headers("Prefer: return=representation")
    @POST("rest/v1/mis_carpetas")
    Call<List<MiCarpeta>> crearCarpeta(@Body Map<String, Object> body);

    @PATCH("rest/v1/mis_carpetas")
    Call<Void> renombrarCarpeta(@Query("id") String filtroId, @Body Map<String, Object> body);

    @DELETE("rest/v1/mis_carpetas")
    Call<Void> eliminarCarpeta(@Query("id") String filtroId);

    // ── ARCHIVOS PERSONALES ───────────────────────────────────────────────────

    /** Archivos en la raíz (sin carpeta) de un usuario */
    @GET("rest/v1/mis_archivos?order=fecha_subida.desc")
    Call<List<MiArchivo>> getMisArchivosSinCarpeta(
            @Query("usuario_id") String usuarioIdFiltro,
            @Query("carpeta_id") String carpetaIdFiltro   // "is.null"
    );

    /** Archivos dentro de una carpeta */
    @GET("rest/v1/mis_archivos?order=fecha_subida.desc")
    Call<List<MiArchivo>> getMisArchivosDeCarpeta(
            @Query("usuario_id") String usuarioIdFiltro,
            @Query("carpeta_id") String carpetaIdFiltro   // "eq.{id}"
    );

    @Headers("Prefer: return=representation")
    @POST("rest/v1/mis_archivos")
    Call<List<MiArchivo>> subirMiArchivo(@Body Map<String, Object> body);

    @DELETE("rest/v1/mis_archivos")
    Call<Void> eliminarMiArchivo(@Query("id") String filtroId);

    // ── MIGRACIÓN DE DATOS (UUID a USER_ID) ──────────────────────────────────
    
    @PATCH("rest/v1/mis_carpetas")
    Call<Void> migrarCarpetas(@Query("usuario_id") String oldUsuarioId, @Body Map<String, Object> body);

    @PATCH("rest/v1/mis_archivos")
    Call<Void> migrarArchivos(@Query("usuario_id") String oldUsuarioId, @Body Map<String, Object> body);
}
