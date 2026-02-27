package com.dam.studybro.utils;

import android.content.Context;
import android.util.Log;

import com.dam.studybro.database.BaseDatosApp;
import com.dam.studybro.network.SupabaseApi;
import com.dam.studybro.network.SupabaseClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

/**
 * Clase de utilidad para sincronizar los datos maestros (Centros, Especialidades, Asignaturas)
 * desde Supabase hacia la base de datos local Room.
 */
public class SyncHelper {

    private static final String TAG = "SyncHelper";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void sincronizarDesdeNube(Context context) {
        executor.execute(() -> {
            try {
                BaseDatosApp db = BaseDatosApp.getInstance(context);
                SupabaseApi api = SupabaseClient.getClient().create(SupabaseApi.class);

                Log.d(TAG, "Iniciando descarga de datos globales desde Supabase...");

                // 1. Sincronizar Especialidades
                Response<java.util.List<com.dam.studybro.database.Especialidad>> respEsp = api.getEspecialidades().execute();
                if (respEsp.isSuccessful() && respEsp.body() != null) {
                    db.especialidadDao().insertarLista(respEsp.body());
                    Log.d(TAG, "Especialidades sincronizadas: " + respEsp.body().size());
                }

                // 2. Sincronizar Centros
                Response<java.util.List<com.dam.studybro.database.Centro>> respCentros = api.getCentros().execute();
                if (respCentros.isSuccessful() && respCentros.body() != null) {
                    db.centroDao().insertarLista(respCentros.body());
                    Log.d(TAG, "Centros sincronizados: " + respCentros.body().size());
                }

                // 3. Sincronizar Asignaturas
                Response<java.util.List<com.dam.studybro.database.Asignatura>> respAsig = api.getAsignaturas().execute();
                if (respAsig.isSuccessful() && respAsig.body() != null) {
                    db.asignaturaDao().insertarLista(respAsig.body());
                    Log.d(TAG, "Asignaturas sincronizadas: " + respAsig.body().size());
                }

                // 4. Sincronizar Vínculos Centro-Especialidad
                Response<java.util.List<com.dam.studybro.database.CentroEspecialidad>> respVinculos = api.getCentroEspecialidades().execute();
                if (respVinculos.isSuccessful() && respVinculos.body() != null) {
                    db.centroEspecialidadDao().insertarLista(respVinculos.body());
                    Log.d(TAG, "Vínculos Centro-Especialidad sincronizados: " + respVinculos.body().size());
                }

                Log.d(TAG, "Sincronización completada con éxito.");

            } catch (Exception e) {
                Log.e(TAG, "Error durante la sincronización: " + e.getMessage());
            }
        });
    }
}
