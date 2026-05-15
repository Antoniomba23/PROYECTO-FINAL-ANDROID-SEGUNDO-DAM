package com.dam.studyfiles.network;

import com.dam.studyfiles.models.Archivo;
import com.dam.studyfiles.models.SubirArchivoRequest;

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

public interface SupabaseApi {

    // ── GET ──────────────────────────────────────────────────────────────────

    /** Todos los archivos, ordenados por fecha descendente */
    @GET("rest/v1/archivos?order=fecha_subida.desc")
    Call<List<Archivo>> getArchivos();

    /** Archivos de una categoría concreta */
    @GET("rest/v1/archivos?order=fecha_subida.desc")
    Call<List<Archivo>> getArchivosPorCategoria(@Query("categoria") String categoria);

    /** Obtener un solo archivo por su ID */
    @GET("rest/v1/archivos")
    Call<List<Archivo>> getArchivoPorId(@Query("id") String eqId);

    /** Buscar archivos por nombre, descripción o categoría */
    @GET("rest/v1/archivos?order=fecha_subida.desc")
    Call<List<Archivo>> buscarArchivosAvanzado(@Query(value = "or", encoded = true) String orQuery);

    // ── COMENTARIOS ──────────────────────────────────────────────────────────

    @GET("rest/v1/comentarios?order=fecha.desc")
    Call<List<com.dam.studyfiles.models.Comentario>> getComentarios(@Query("archivo_id") String eqArchivoId);

    @Headers("Prefer: return=minimal")
    @POST("rest/v1/comentarios")
    Call<Void> crearComentario(@Body com.dam.studyfiles.models.Comentario comentario);

    @PATCH("rest/v1/comentarios")
    Call<Void> actualizarComentario(
            @Query("id") String filtroId,
            @Body Map<String, Object> campos
    );

    @DELETE("rest/v1/comentarios")
    Call<Void> eliminarComentario(@Query("id") String filtroId);

    // ── POST ─────────────────────────────────────────────────────────────────

    /** Insertar nuevo archivo en la tabla */
    @Headers("Prefer: return=minimal")
    @POST("rest/v1/archivos")
    Call<Void> subirArchivo(@Body SubirArchivoRequest request);

    // ── PATCH ────────────────────────────────────────────────────────────────

    /** Actualizar likes, dislikes o reportes */
    @PATCH("rest/v1/archivos")
    Call<Void> actualizarArchivo(
            @Query("id") String filtroId,
            @Body Map<String, Object> campos
    );

    // ── DELETE ───────────────────────────────────────────────────────────────

    /** Eliminar archivo (cuando dislikes >= 10) */
    @DELETE("rest/v1/archivos")
    Call<Void> eliminarArchivo(@Query("id") String filtroId);
}
