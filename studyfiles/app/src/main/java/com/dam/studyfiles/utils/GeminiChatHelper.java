package com.dam.studyfiles.utils;

import android.os.Handler;
import android.os.Looper;

import com.dam.studyfiles.models.MensajeChat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GeminiChatHelper {

    private static final String API_KEY = "AIzaSyAemK4le918ROYh2OyOIhPq1WYt6lx1zgI";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

    public interface GeminiCallback {
        void onSuccess(String result);
        void onError(String error);
    }

    public static void enviarMensajeChat(List<MensajeChat> historial, String nuevoMensaje, GeminiCallback callback) {
        OkHttpClient client = new OkHttpClient();

        try {
            JSONObject jsonRequest = new JSONObject();
            JSONArray contentsArray = new JSONArray();

            // Mensaje de sistema oculto al principio del historial para darle contexto a la IA
            String systemPrompt = "Eres un Asistente y Tutor Académico experto para la app StudyFiles. " +
                    "Respondes de forma amable, clara y didáctica para ayudar a estudiantes. " +
                    "Mantén tus respuestas relativamente cortas y directas.";

            // Agregar el prompt de sistema como el primer mensaje (role user, luego model lo acepta)
            JSONObject sysUser = new JSONObject();
            sysUser.put("role", "user");
            JSONArray sysPartsUser = new JSONArray();
            sysPartsUser.put(new JSONObject().put("text", systemPrompt));
            sysUser.put("parts", sysPartsUser);
            contentsArray.put(sysUser);

            JSONObject sysModel = new JSONObject();
            sysModel.put("role", "model");
            JSONArray sysPartsModel = new JSONArray();
            sysPartsModel.put(new JSONObject().put("text", "Entendido, soy el tutor de StudyFiles. ¿En qué te ayudo?"));
            sysModel.put("parts", sysPartsModel);
            contentsArray.put(sysModel);

            // Agregar el historial de la base de datos
            if (historial != null) {
                for (MensajeChat msg : historial) {
                    JSONObject contentObj = new JSONObject();
                    contentObj.put("role", msg.rol); // "user" o "model"
                    JSONArray partsArray = new JSONArray();
                    partsArray.put(new JSONObject().put("text", msg.mensaje));
                    contentObj.put("parts", partsArray);
                    contentsArray.put(contentObj);
                }
            }

            // Agregar el mensaje actual del usuario
            JSONObject currentContent = new JSONObject();
            currentContent.put("role", "user");
            JSONArray currentParts = new JSONArray();
            currentParts.put(new JSONObject().put("text", nuevoMensaje));
            currentContent.put("parts", currentParts);
            contentsArray.put(currentContent);

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
                        enviarError(callback, "Error del servidor de IA (" + response.code() + ")");
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
                            enviarError(callback, "La IA no devolvió ninguna respuesta válida.");
                        }
                    } catch (JSONException e) {
                        enviarError(callback, "Error procesando la respuesta de la IA.");
                    }
                }
            });

        } catch (JSONException e) {
            callback.onError("Error al preparar la consulta.");
        }
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
