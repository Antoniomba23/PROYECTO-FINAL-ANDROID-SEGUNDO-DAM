package com.dam.studyfiles.utils;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.UUID;

/**
 * Genera y persiste un UUID único por dispositivo.
 * Se usa como identificador anónimo para "Mi Nube" sin login.
 */
public class DispositivoUtils {

    private static final String PREFS = "studyfiles_prefs";
    private static final String KEY_UUID = "device_uuid";

    public static String getUUID(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String uuid = prefs.getString(KEY_UUID, null);
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
            prefs.edit().putString(KEY_UUID, uuid).apply();
        }
        return uuid;
    }

    public static void setUsuario(Context context, String id, String nombre) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString("usuario_id", id)
                .putString("usuario_nombre", nombre)
                .apply();
    }

    public static String getUsuarioId(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString("usuario_id", null);
    }

    public static String getUsuarioNombre(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString("usuario_nombre", "Anónimo");
    }

    public static boolean isLogged(Context context) {
        return getUsuarioId(context) != null;
    }

    public static void logout(Context context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .remove("usuario_id")
                .remove("usuario_nombre")
                .apply();
    }

    public static boolean isDarkMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return prefs.getBoolean("dark_mode", false);
    }

    public static void setDarkMode(Context context, boolean dark) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putBoolean("dark_mode", dark).apply();
    }
}
