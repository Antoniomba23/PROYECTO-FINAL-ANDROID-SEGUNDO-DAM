package com.dam.studybro.network;

import com.dam.studybro.database.Publicacion;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SupabaseApi {
    
    // PUBLICACIONES
    
    // Obtener todas las publicaciones (equivalente a obtenerTodas en Room)
    @GET("publicaciones?select=*&order=fecha_subida.desc")
    Call<List<Publicacion>> getPublicaciones();
    
    // Obtener publicaciones filtradas por asignaturaId
    @GET("publicaciones?select=*&order=fecha_subida.desc")
    Call<List<Publicacion>> getPublicacionesPorAsignatura(@Query("asignatura_id") String eqAsignaturaId);
    
    // Obtener una publicación específica por ID
    @GET("publicaciones?select=*")
    Call<List<Publicacion>> getPublicacionPorId(@Query("id") String eqId);

    // Obtener publicaciones por una lista de IDs (formato in.(1,2,3))
    @GET("publicaciones?select=*&order=fecha_subida.desc")
    Call<List<Publicacion>> getPublicacionesPorIds(@Query("id") String inIds);

    // Obtener publicaciones de un usuario específico
    @GET("publicaciones?select=*&order=fecha_subida.desc")
    Call<List<Publicacion>> getPublicacionesPorUsuario(@Query("usuario_id") String eqUsuarioId);
    
    // Crear una nueva publicación
    @POST("publicaciones")
    Call<Void> crearPublicacion(@Body com.dam.studybro.network.CrearPublicacionRequest publicacion);

    // Actualizar una publicación (PATCH)
    @retrofit2.http.PATCH("publicaciones")
    Call<Void> actualizarPublicacion(@Query("id") String eqId, @Body Publicacion publicacion);

    // Eliminar una publicación (DELETE)
    @retrofit2.http.DELETE("publicaciones")
    Call<Void> eliminarPublicacion(@Query("id") String eqId);

    // ==========================================
    // COMENTARIOS
    // ==========================================

    // Obtener comentarios de una publicación
    @GET("comentarios?select=*&order=fecha.asc")
    Call<List<com.dam.studybro.database.Comentario>> getComentarios(@Query("publicacion_id") String eqPublicacionId);
    
    // Añadir comentario
    @POST("comentarios")
    Call<Void> crearComentario(@Body com.dam.studybro.network.CrearComentarioRequest comentario);

    // Actualizar Comentario (PATCH)
    @retrofit2.http.PATCH("comentarios")
    Call<Void> actualizarComentario(@Query("id") String eqId, @Body com.dam.studybro.network.CrearComentarioRequest comentario);

    // Eliminar Comentario
    @retrofit2.http.DELETE("comentarios")
    Call<Void> eliminarComentario(@Query("id") String eqId);

    // ==========================================
    // VALORACIONES DE CENTROS
    // ==========================================

    // Obtener valoraciones de un centro
    @GET("valoraciones_centro?select=*&order=fecha.desc")
    Call<List<com.dam.studybro.database.ValoracionCentro>> getValoracionesCentro(@Query("centro_id") String eqCentroId);

    // Obtener la valoración de un usuario específico para un centro
    @GET("valoraciones_centro?select=*")
    Call<List<com.dam.studybro.database.ValoracionCentro>> getValoracionPropia(@Query("centro_id") String eqCentroId, @Query("usuario_id") String eqUsuarioId);

    // Añadir valoración de centro
    @POST("valoraciones_centro")
    Call<Void> crearValoracionCentro(@Body com.dam.studybro.network.CrearValoracionRequest valoracion);

    // Actualizar valoración de centro (PATCH)
    @retrofit2.http.PATCH("valoraciones_centro")
    Call<Void> actualizarValoracionCentro(@Query("id") String eqId, @Body com.dam.studybro.network.CrearValoracionRequest valoracion);

    // Eliminar valoración de centro
    @retrofit2.http.DELETE("valoraciones_centro")
    Call<Void> eliminarValoracionCentro(@Query("id") String eqId);
    // ==========================================
    // CENTROS, ESPECIALIDADES Y ASIGNATURAS
    // ==========================================

    @GET("centros?select=*&order=nombre.asc")
    Call<List<com.dam.studybro.database.Centro>> getCentros();

    @POST("centros")
    Call<Void> crearCentro(@Body com.dam.studybro.database.Centro centro);

    @GET("especialidades?select=*&order=nombre.asc")
    Call<List<com.dam.studybro.database.Especialidad>> getEspecialidades();

    @POST("especialidades")
    Call<Void> crearEspecialidad(@Body com.dam.studybro.database.Especialidad especialidad);

    @GET("asignaturas?select=*&order=nombre.asc")
    Call<List<com.dam.studybro.database.Asignatura>> getAsignaturas();

    @POST("asignaturas")
    Call<Void> crearAsignatura(@Body com.dam.studybro.database.Asignatura asignatura);

    @GET("centro_especialidades?select=*")
    Call<List<com.dam.studybro.database.CentroEspecialidad>> getCentroEspecialidades();

    @POST("centro_especialidades")
    Call<Void> crearCentroEspecialidad(@Body com.dam.studybro.database.CentroEspecialidad ce);

    // ==========================================
    // GESTIÓN DE USUARIOS
    // ==========================================

    @GET("usuarios?select=*&order=nombre.asc")
    Call<List<com.dam.studybro.database.Usuario>> getUsuarios();

    @retrofit2.http.PATCH("usuarios")
    Call<Void> actualizarUsuario(@Query("id") String eqId, @Body com.dam.studybro.database.Usuario usuario);

    @retrofit2.http.DELETE("usuarios")
    Call<Void> eliminarUsuario(@Query("id") String eqId);
}
