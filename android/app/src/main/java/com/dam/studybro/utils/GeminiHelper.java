package com.dam.studybro.utils;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GeminiHelper {

    private static final String API_KEY = "AIzaSyAemK4le918ROYh2OyOIhPq1WYt6lx1zgI";
    // Usamos el modelo más reciente y estable para 2026
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

    public interface GeminiCallback {
        void onSuccess(String result);
        void onError(String error);
    }

    public static void resumirPublicacion(String titulo, String contenido, GeminiCallback callback) {
        OkHttpClient client = new OkHttpClient();

        // Prompt estructurado para nuestro bot experto
        String prompt = "Eres un Asistente y Tutor Experto para estudiantes de grado superior y universidad. " +
                "Por favor, lee la siguiente publicación compartida por un alumno y elabora un RESUMEN DIDÁCTICO, CLARO Y BIEN ESTRUCTURADO " +
                "con puntos clave e ideas principales para facilitar su estudio. \\n\\n" +
                "TITULO: " + titulo + "\\n" +
                "CONTENIDO: \\n" + contenido;

        try {
            // Estructura JSON que requiere la API de Gemini (Role/Parts)
            JSONObject jsonRequest = new JSONObject();
            JSONArray contentsArray = new JSONArray();
            JSONObject contentObj = new JSONObject();
            JSONArray partsArray = new JSONArray();
            JSONObject partObj = new JSONObject();

            partObj.put("text", prompt);
            partsArray.put(partObj);
            
            contentObj.put("role", "user");
            contentObj.put("parts", partsArray);
            
            contentsArray.put(contentObj);
            jsonRequest.put("contents", contentsArray);

            RequestBody body = RequestBody.create(
                    jsonRequest.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

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
                        enviarError(callback, "Error de servidor HTTP " + response.code() + ": " + response.message());
                        return;
                    }

                    String responseData = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseData);
                        JSONArray candidates = jsonResponse.optJSONArray("candidates");
                        if (candidates != null && candidates.length() > 0) {
                            JSONObject firstCandidate = candidates.getJSONObject(0);
                            JSONObject content = firstCandidate.optJSONObject("content");
                            if (content != null) {
                                JSONArray parts = content.optJSONArray("parts");
                                if (parts != null && parts.length() > 0) {
                                    String textResult = parts.getJSONObject(0).optString("text", "");
                                    enviarExito(callback, textResult);
                                    return;
                                }
                            }
                        }
                        enviarError(callback, "El formato de respuesta de la IA no es el esperado.");
                    } catch (JSONException e) {
                        enviarError(callback, "Error parseando la respuesta: " + e.getMessage());
                    }
                }
            });

        } catch (JSONException e) {
            callback.onError("Error al construir la petición: " + e.getMessage());
        }
    }

    public static void analizarOpiniones(String nombreCentro, String opiniones, GeminiCallback callback) {
        OkHttpClient client = new OkHttpClient();

        String prompt = "Eres un analista experto en educación. " +
                "A continuación tienes una lista de opiniones reales de estudiantes sobre un centro educativo. " +
                "Por favor, elabora un RESUMEN EQUILIBRADO del centro '" + nombreCentro + "' destacando: " +
                "1. Lo más valorado (puntos fuertes). \\n" +
                "2. Aspectos a mejorar mencionados. \\n" +
                "3. Una conclusión general sobre el clima del centro. \\n\\n" +
                "OPINIONES: \\n" + opiniones;

        try {
            JSONObject jsonRequest = new JSONObject();
            JSONArray contentsArray = new JSONArray();
            JSONObject contentObj = new JSONObject();
            JSONArray partsArray = new JSONArray();
            JSONObject partObj = new JSONObject();

            partObj.put("text", prompt);
            partsArray.put(partObj);
            contentObj.put("role", "user");
            contentObj.put("parts", partsArray);
            contentsArray.put(contentObj);
            jsonRequest.put("contents", contentsArray);

            RequestBody body = RequestBody.create(
                    jsonRequest.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

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
                        enviarError(callback, "Error de IA (" + response.code() + ")");
                        return;
                    }

                    String responseData = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseData);
                        JSONArray candidates = jsonResponse.optJSONArray("candidates");
                        if (candidates != null && candidates.length() > 0) {
                            String textResult = candidates.getJSONObject(0)
                                    .getJSONObject("content")
                                    .getJSONArray("parts")
                                    .getJSONObject(0).optString("text", "");
                            enviarExito(callback, textResult);
                        } else {
                            enviarError(callback, "Sin respuesta de la IA.");
                        }
                    } catch (JSONException e) {
                        enviarError(callback, "Error de procesado.");
                    }
                }
            });

        } catch (JSONException e) {
            callback.onError("Error de petición.");
        }
    }

    public static void mejorarPublicacion(String tituloActual, String contenidoActual, String asignatura, GeminiCallback callback) {
        OkHttpClient client = new OkHttpClient();

        String prompt = "Eres un Asistente Editorial Académico. Tu objetivo es ayudar a un estudiante a que su publicación sea más atractiva y profesional.\\n\\n" +
                "DATOS ACTUALES:\\n" +
                "- Título: " + tituloActual + "\\n" +
                "- Contenido: " + contenidoActual + "\\n" +
                "- Asignatura: " + asignatura + "\\n\\n" +
                "POR FAVOR, RESPONDE ÚNICAMENTE CON UN JSON USANDO EXACTAMENTE ESTE FORMATO (no añadas explicaciones fuera del JSON):\\n" +
                "{\\n" +
                "  \\\"titulo_sugerido\\\": \\\"(título breve y profesional)\\\",\\n" +
                "  \\\"descripcion_mejorada\\\": \\\"(descripcion con mejor gramática y estructura)\\\",\\n" +
                "  \\\"consejo\\\": \\\"(un breve consejo para el estudiante)\\\"\\n" +
                "}";

        try {
            JSONObject jsonRequest = new JSONObject();
            JSONArray contentsArray = new JSONArray();
            JSONObject contentObj = new JSONObject();
            JSONArray partsArray = new JSONArray();
            JSONObject partObj = new JSONObject();

            partObj.put("text", prompt);
            partsArray.put(partObj);
            contentObj.put("role", "user");
            contentObj.put("parts", partsArray);
            contentsArray.put(contentObj);
            jsonRequest.put("contents", contentsArray);

            RequestBody body = RequestBody.create(
                    jsonRequest.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

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
                        enviarError(callback, "Error de IA (" + response.code() + ")");
                        return;
                    }

                    String responseData = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseData);
                        JSONArray candidates = jsonResponse.optJSONArray("candidates");
                        if (candidates != null && candidates.length() > 0) {
                            String textResult = candidates.getJSONObject(0)
                                    .getJSONObject("content")
                                    .getJSONArray("parts")
                                    .getJSONObject(0).optString("text", "");
                            
                            // Intentar limpiar el JSON si la IA puso backticks
                            String jsonLimpio = textResult.trim();
                            if (jsonLimpio.startsWith("```json")) {
                                jsonLimpio = jsonLimpio.substring(7, jsonLimpio.length() - 3).trim();
                            } else if (jsonLimpio.startsWith("```")) {
                                jsonLimpio = jsonLimpio.substring(3, jsonLimpio.length() - 3).trim();
                            }
                            
                            enviarExito(callback, jsonLimpio);
                        } else {
                            enviarError(callback, "Sin respuesta de la IA.");
                        }
                    } catch (JSONException e) {
                        enviarError(callback, "Error de procesado JSON.");
                    }
                }
            });

        } catch (JSONException e) {
            callback.onError("Error de petición.");
        }
    }

    public static void pedirAyudaGeneral(String contexto, String pregunta, GeminiCallback callback) {
        OkHttpClient client = new OkHttpClient();

        String prompt = "Eres un Asistente y Tutor Académico para la aplicación StudyBro.\\n" +
                "CONTEXTO: " + contexto + "\\n" +
                "PREGUNTA DEL USUARIO: " + pregunta + "\\n\\n" +
                "Por favor, responde de forma amable, motivadora y útil para el estudiante.";

        try {
            JSONObject jsonRequest = new JSONObject();
            JSONArray contentsArray = new JSONArray();
            JSONObject contentObj = new JSONObject();
            JSONArray partsArray = new JSONArray();
            JSONObject partObj = new JSONObject();

            partObj.put("text", prompt);
            partsArray.put(partObj);
            contentObj.put("role", "user");
            contentObj.put("parts", partsArray);
            contentsArray.put(contentObj);
            jsonRequest.put("contents", contentsArray);

            RequestBody body = RequestBody.create(
                    jsonRequest.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) { enviarError(callback, "Error de red"); }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) { enviarError(callback, "Error de IA"); return; }
                    try {
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        String result = jsonResponse.getJSONArray("candidates")
                                .getJSONObject(0).getJSONObject("content")
                                .getJSONArray("parts").getJSONObject(0).getString("text");
                        enviarExito(callback, result);
                    } catch (Exception e) { enviarError(callback, "Error procesando respuesta"); }
                }
            });
        } catch (JSONException e) { callback.onError("Error de petición"); }
    }

    public static void pedirAyudaEspecializada(String titulo, String contenido, String tipoAyuda, GeminiCallback callback) {
        OkHttpClient client = new OkHttpClient();

        String instruccion = "";
        switch (tipoAyuda) {
            case "EXPLICAR":
                instruccion = "Por favor, explica los conceptos más difíciles de esta publicación de forma muy sencilla, como si fueras un tutor personal.";
                break;
            case "QUIZ":
                instruccion = "Genera 3 preguntas de repaso rápidas con sus soluciones basándote en este contenido para que el alumno pueda autoevaluarse.";
                break;
            default:
                instruccion = "Elabora un resumen didáctico y estructurado con los puntos clave.";
                break;
        }

        String prompt = "Eres un Tutor Académico Inteligente.\\n" +
                "PUBLICACIÓN: " + titulo + "\\n" +
                "CONTENIDO: " + contenido + "\\n\\n" +
                "TAREA: " + instruccion;

        try {
            JSONObject jsonRequest = new JSONObject();
            JSONArray contentsArray = new JSONArray();
            JSONObject contentObj = new JSONObject();
            JSONArray partsArray = new JSONArray();
            JSONObject partObj = new JSONObject();

            partObj.put("text", prompt);
            partsArray.put(partObj);
            contentObj.put("role", "user");
            contentObj.put("parts", partsArray);
            contentsArray.put(contentObj);
            jsonRequest.put("contents", contentsArray);

            RequestBody body = RequestBody.create(
                    jsonRequest.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) { enviarError(callback, "Error de red"); }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) { enviarError(callback, "Error de IA"); return; }
                    try {
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        String result = jsonResponse.getJSONArray("candidates")
                                .getJSONObject(0).getJSONObject("content")
                                .getJSONArray("parts").getJSONObject(0).getString("text");
                        enviarExito(callback, result);
                    } catch (Exception e) { enviarError(callback, "Error procesando respuesta"); }
                }
            });
        } catch (JSONException e) { callback.onError("Error de petición"); }
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
