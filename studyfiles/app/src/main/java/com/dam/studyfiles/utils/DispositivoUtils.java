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

    public static String obtenerExtension(String nombre) {
        if (nombre != null && nombre.contains("."))
            return nombre.substring(nombre.lastIndexOf(".") + 1).toLowerCase();
        return "bin";
    }

    public static String extensionDesdeMime(String mime) {
        if (mime == null) return "bin";
        switch (mime) {
            case "application/pdf":  return "pdf";
            case "image/jpeg":       return "jpg";
            case "image/png":        return "png";
            case "image/gif":        return "gif";
            case "image/webp":       return "webp";
            case "application/msword": return "doc";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document": return "docx";
            case "application/vnd.ms-powerpoint": return "ppt";
            case "application/vnd.openxmlformats-officedocument.presentationml.presentation": return "pptx";
            case "application/vnd.ms-excel": return "xls";
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet": return "xlsx";
            case "text/plain":       return "txt";
            case "application/zip":  return "zip";
            default: return "bin";
        }
    }

    public static String getMimeType(String ext) {
        if (ext == null) return "*/*";
        switch (ext.toLowerCase()) {
            case "pdf":  return "application/pdf";
            case "doc":  return "application/msword";
            case "docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "ppt":  return "application/vnd.ms-powerpoint";
            case "pptx": return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "xls":  return "application/vnd.ms-excel";
            case "xlsx": return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "png":  return "image/png";
            case "jpg":  case "jpeg": return "image/jpeg";
            case "txt":  return "text/plain";
            case "zip":  return "application/zip";
            default:     return "*/*";
        }
    }
}
