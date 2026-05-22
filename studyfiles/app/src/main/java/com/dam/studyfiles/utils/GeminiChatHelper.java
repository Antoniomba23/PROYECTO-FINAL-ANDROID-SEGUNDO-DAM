package com.dam.studyfiles.utils;

import android.os.Handler;
import android.os.Looper;

import com.dam.studyfiles.models.Archivo;
import com.dam.studyfiles.models.MensajeChat;
import com.dam.studyfiles.network.SupabaseClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GeminiChatHelper {

    private static final String API_KEY = com.dam.studyfiles.BuildConfig.GEMINI_API_KEY;
    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

    public interface GeminiCallback {
        void onSuccess(String result);
        void onError(String error);
    }

    /**
     * Estrategia de búsqueda y respuesta contextual:
     * 1. Busca en Supabase archivos relacionados con el mensaje del usuario.
     * 2. Inyecta esos resultados como contexto en la consulta.
     * 3. Obtiene la respuesta contextual con información real de la app.
     */
    public static void enviarMensajeChat(List<MensajeChat> historial, String nuevoMensaje, GeminiCallback callback) {
        // Extraer palabras clave del mensaje (ignorar palabras vacías cortas)
        String[] palabras = nuevoMensaje.split("\\s+");
        StringBuilder orQueryBuilder = new StringBuilder("(");
        boolean primero = true;
        String[] stopWords = {"en", "el", "la", "los", "las", "un", "una", "de", "del", "al",
                "y", "o", "que", "a", "por", "con", "para", "hay", "buscar", "busca",
                "libro", "libros", "archivo", "archivos", "como", "si", "no", "me", "se",
                "su", "mi", "tu", "nos", "es", "son", "fue", "ser", "esta", "estas",
                "estos", "aquellos", "donde", "cuando", "quien", "quienes", "cual", "cuales",
                "puedes", "puedo", "hola", "tutor", "ia", "encuentra", "encuentralo", "dime",
                "sobre", "acerca", "llamado", "titulado", "nombre", "categoria", "ayuda"};
        java.util.Set<String> stopSet = new java.util.HashSet<>(java.util.Arrays.asList(stopWords));

        for (String p : palabras) {
            String pal = p.trim().toLowerCase().replaceAll("[^a-záéíóúüñ0-9]", "");
            if (pal.length() >= 3 && !stopSet.contains(pal)) {
                if (!primero) orQueryBuilder.append(",");
                
                // Normalización para búsqueda insensible a acentos (Postgres ilike es sensible a acentos)
                String palSinTildes = pal.replaceAll("[áàäâ]", "a")
                                         .replaceAll("[éèëê]", "e")
                                         .replaceAll("[íìïî]", "i")
                                         .replaceAll("[óòöô]", "o")
                                         .replaceAll("[úùüû]", "u");

                orQueryBuilder.append("nombre.ilike.*").append(pal).append("*")
                        .append(",descripcion.ilike.*").append(pal).append("*")
                        .append(",categoria.ilike.*").append(pal).append("*");
                
                // Si la palabra tenía tildes, añadimos también la versión sin tildes a la búsqueda
                if (!pal.equals(palSinTildes)) {
                    orQueryBuilder.append(",nombre.ilike.*").append(palSinTildes).append("*")
                            .append(",descripcion.ilike.*").append(palSinTildes).append("*");
                }
                primero = false;
            }
        }
        orQueryBuilder.append(")");

        // Si no hay palabras clave útiles, buscar con el mensaje completo recortado
        String orQuery;
        if (primero) {
            String q = nuevoMensaje.trim().toLowerCase();
            String qSinTildes = q.replaceAll("[áàäâ]", "a")
                                .replaceAll("[éèëê]", "e")
                                .replaceAll("[íìïî]", "i")
                                .replaceAll("[óòöô]", "o")
                                .replaceAll("[úùüû]", "u");
            orQuery = "(nombre.ilike.*" + q + "*,descripcion.ilike.*" + q + "*,nombre.ilike.*" + qSinTildes + "*)";
        } else {
            orQuery = orQueryBuilder.toString();
        }

        SupabaseClient.getApi().buscarArchivosAvanzado(orQuery)
                .enqueue(new retrofit2.Callback<List<Archivo>>() {
                    @Override
                    public void onResponse(retrofit2.Call<List<Archivo>> call,
                                           retrofit2.Response<List<Archivo>> response) {
                        String contexto = "";
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("\n\n[ARCHIVOS ENCONTRADOS EN STUDYFILES - menciónalos usando el formato exacto [file:ID:NOMBRE]]:\n");
                            for (Archivo a : response.body()) {
                                sb.append("• [file:").append(a.id).append(":").append(a.nombre).append("]");
                                if (a.descripcion != null && !a.descripcion.isEmpty())
                                    sb.append(" — ").append(a.descripcion);
                                if (a.categoria != null)
                                    sb.append(" (Categoría: ").append(a.categoria).append(")");
                                sb.append("\n");
                            }
                            contexto = sb.toString();
                        }
                        enviarConsulta(historial, nuevoMensaje, contexto, callback);
                    }

                    @Override
                    public void onFailure(retrofit2.Call<List<Archivo>> call, Throwable t) {
                        // Si falla la búsqueda, se responde sin contexto
                        enviarConsulta(historial, nuevoMensaje, "", callback);
                    }
                });
    }

    private static void enviarConsulta(List<MensajeChat> historial,
                                       String nuevoMensaje,
                                       String contextoArchivos,
                                       GeminiCallback callback) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        try {
            JSONArray contentsArray = new JSONArray();

            // --- System prompt (turno user + ack model) ---
            String systemPrompt = "Eres un Tutor Académico experto integrado en la app StudyFiles. " +
                    "Ayudas a estudiantes a encontrar apuntes, resúmenes y materiales de estudio. " +
                    "Cuando se te proporcionen archivos disponibles en la app, debes mencionarlos usando " +
                    "ESTRICTAMENTE este formato: [file:ID:NOMBRE]. Ejemplo: [file:14:Apuntes de Java]. " +
                    "Si no hay archivos relevantes, responde con tu conocimiento general de forma didáctica.";

            addTurn(contentsArray, "user", systemPrompt);
            addTurn(contentsArray, "model",
                    "Entendido. Soy el tutor de StudyFiles. Usaré [file:ID:NOMBRE] para referenciar archivos.");

            // --- Historial previo ---
            if (historial != null) {
                for (MensajeChat msg : historial) {
                    addTurn(contentsArray, msg.rol, msg.mensaje);
                }
            }

            // --- Mensaje actual + contexto RAG ---
            String mensajeConContexto = nuevoMensaje;
            if (!contextoArchivos.isEmpty()) {
                mensajeConContexto = nuevoMensaje + contextoArchivos;
            }
            addTurn(contentsArray, "user", mensajeConContexto);

            // --- Construcción del request ---
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("contents", contentsArray);

            JSONObject genConfig = new JSONObject();
            genConfig.put("temperature", 0.7);
            genConfig.put("maxOutputTokens", 1024);
            jsonRequest.put("generationConfig", genConfig);

            RequestBody body = RequestBody.create(
                    jsonRequest.toString(),
                    MediaType.parse("application/json; charset=utf-8"));

            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    enviarError(callback, "Error de red: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        String err = response.body() != null ? response.body().string() : "Sin cuerpo de error";
                        if (err.length() > 200) err = err.substring(0, 200);
                        enviarError(callback, "Error del Tutor (" + response.code() + "): " + err);
                        return;
                    }
                    String data = response.body().string();
                    try {
                        JSONObject json = new JSONObject(data);
                        JSONArray candidates = json.optJSONArray("candidates");
                        if (candidates != null && candidates.length() > 0) {
                            JSONObject content = candidates.getJSONObject(0).optJSONObject("content");
                            if (content != null) {
                                JSONArray parts = content.optJSONArray("parts");
                                if (parts != null && parts.length() > 0) {
                                    String text = parts.getJSONObject(0).optString("text", "");
                                    enviarExito(callback, text);
                                    return;
                                }
                            }
                        }
                        enviarError(callback, "Respuesta vacía del Tutor.");
                    } catch (JSONException e) {
                        enviarError(callback, "Error de parseo: " + e.getMessage());
                    }
                }
            });

        } catch (JSONException e) {
            callback.onError("Error preparando la consulta: " + e.getMessage());
        }
    }

    // Helper para añadir un turno al array de contents
    private static void addTurn(JSONArray arr, String role, String text) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("role", role);
        obj.put("parts", new JSONArray().put(new JSONObject().put("text", text)));
        arr.put(obj);
    }

    private static void enviarExito(GeminiCallback callback, String resultado) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (callback != null) callback.onSuccess(resultado);
        });
    }

    private static void enviarError(GeminiCallback callback, String error) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (callback != null) callback.onError(error);
        });
    }
}
