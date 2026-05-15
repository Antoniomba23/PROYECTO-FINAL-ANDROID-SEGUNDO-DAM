package com.dam.studyfiles.utils;

import android.os.Handler;
import android.os.Looper;

import com.dam.studyfiles.models.MensajeChat;
import com.dam.studyfiles.models.Archivo;
import com.dam.studyfiles.network.SupabaseClient;

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

    private static final String API_KEY = com.dam.studyfiles.BuildConfig.GEMINI_API_KEY;
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

    public interface GeminiCallback {
        void onSuccess(String result);
        void onError(String error);
    }

    public static void enviarMensajeChat(List<MensajeChat> historial, String nuevoMensaje, GeminiCallback callback) {
        hacerPeticionGemini(historial, nuevoMensaje, null, null, callback);
    }

    private static void hacerPeticionGemini(List<MensajeChat> historial, String nuevoMensaje, JSONObject previousFunctionCall, JSONObject functionResponse, GeminiCallback callback) {
        OkHttpClient client = new OkHttpClient();

        try {
            JSONObject jsonRequest = new JSONObject();
            JSONArray contentsArray = new JSONArray();

            String systemPrompt = "Eres un Asistente y Tutor Académico experto para la app StudyFiles. " +
                    "Respondes de forma amable, clara y didáctica para ayudar a estudiantes. " +
                    "Tienes acceso a una herramienta para buscar archivos en la base de datos. " +
                    "IMPORTANTE: Cuando encuentres archivos usando buscarArchivos, DEBES responder al usuario listando los resultados usando ESTRICTAMENTE este formato por cada archivo: [file:ID_DEL_ARCHIVO:NOMBRE_DEL_ARCHIVO]. No uses Markdown para enlaces, usa SOLO ese formato exacto de corchetes, por ejemplo: '[file:14:Apuntes de Java]'.";

            JSONObject sysUser = new JSONObject();
            sysUser.put("role", "user");
            JSONArray sysPartsUser = new JSONArray();
            sysPartsUser.put(new JSONObject().put("text", systemPrompt));
            sysUser.put("parts", sysPartsUser);
            contentsArray.put(sysUser);

            JSONObject sysModel = new JSONObject();
            sysModel.put("role", "model");
            JSONArray sysPartsModel = new JSONArray();
            sysPartsModel.put(new JSONObject().put("text", "Entendido, soy el tutor de StudyFiles. Usaré el formato [file:id:nombre] si encuentro archivos."));
            sysModel.put("parts", sysPartsModel);
            contentsArray.put(sysModel);

            if (historial != null) {
                for (MensajeChat msg : historial) {
                    JSONObject contentObj = new JSONObject();
                    contentObj.put("role", msg.rol);
                    JSONArray partsArray = new JSONArray();
                    partsArray.put(new JSONObject().put("text", msg.mensaje));
                    contentObj.put("parts", partsArray);
                    contentsArray.put(contentObj);
                }
            }

            JSONObject currentContent = new JSONObject();
            currentContent.put("role", "user");
            JSONArray currentParts = new JSONArray();
            currentParts.put(new JSONObject().put("text", nuevoMensaje));
            currentContent.put("parts", currentParts);
            contentsArray.put(currentContent);

            if (previousFunctionCall != null && functionResponse != null) {
                // Agregar el call del modelo
                JSONObject callContent = new JSONObject();
                callContent.put("role", "model");
                JSONArray callParts = new JSONArray();
                callParts.put(new JSONObject().put("functionCall", previousFunctionCall));
                callContent.put("parts", callParts);
                contentsArray.put(callContent);

                // Agregar la respuesta de la funcion
                JSONObject respContent = new JSONObject();
                respContent.put("role", "function");
                JSONArray respParts = new JSONArray();
                respParts.put(new JSONObject().put("functionResponse", functionResponse));
                respContent.put("parts", respParts);
                contentsArray.put(respContent);
            }

            jsonRequest.put("contents", contentsArray);

            // Tools (Function Calling)
            JSONArray toolsArray = new JSONArray();
            JSONObject toolObj = new JSONObject();
            JSONArray funcDecls = new JSONArray();
            
            JSONObject funcObj = new JSONObject();
            funcObj.put("name", "buscarArchivos");
            funcObj.put("description", "Busca apuntes, resúmenes, exámenes o archivos en la base de datos de la app.");
            
            JSONObject paramsObj = new JSONObject();
            paramsObj.put("type", "OBJECT");
            JSONObject propsObj = new JSONObject();
            JSONObject busqObj = new JSONObject();
            busqObj.put("type", "STRING");
            busqObj.put("description", "El término de búsqueda, ej. 'java', 'matematicas'");
            propsObj.put("busqueda", busqObj);
            paramsObj.put("properties", propsObj);
            paramsObj.put("required", new JSONArray().put("busqueda"));
            
            funcObj.put("parameters", paramsObj);
            funcDecls.put(funcObj);
            toolObj.put("functionDeclarations", funcDecls);
            toolsArray.put(toolObj);
            
            jsonRequest.put("tools", toolsArray);

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
                            JSONObject part = candidates.getJSONObject(0)
                                    .getJSONObject("content")
                                    .getJSONArray("parts")
                                    .getJSONObject(0);
                                    
                            if (part.has("functionCall")) {
                                JSONObject functionCall = part.getJSONObject("functionCall");
                                String name = functionCall.getString("name");
                                if ("buscarArchivos".equals(name)) {
                                    String busqueda = functionCall.getJSONObject("args").optString("busqueda", "");
                                    ejecutarBusquedaSupaBase(busqueda, historial, nuevoMensaje, functionCall, callback);
                                }
                            } else {
                                String textResult = part.optString("text", "");
                                enviarExito(callback, textResult);
                            }
                        } else {
                            enviarError(callback, "Respuesta vacía.");
                        }
                    } catch (JSONException e) {
                        enviarError(callback, "Error de parseo.");
                    }
                }
            });

        } catch (JSONException e) {
            callback.onError("Error al preparar la consulta.");
        }
    }

    private static void ejecutarBusquedaSupaBase(String busqueda, List<MensajeChat> historial, String nuevoMensaje, JSONObject previousFunctionCall, GeminiCallback callback) {
        SupabaseClient.getApi().buscarArchivos("ilike.%" + busqueda + "%").enqueue(new retrofit2.Callback<List<Archivo>>() {
            @Override
            public void onResponse(retrofit2.Call<List<Archivo>> call, retrofit2.Response<List<Archivo>> response) {
                try {
                    JSONObject functionResponse = new JSONObject();
                    functionResponse.put("name", "buscarArchivos");
                    JSONObject responseBody = new JSONObject();
                    
                    if (response.isSuccessful() && response.body() != null) {
                        JSONArray resultados = new JSONArray();
                        for (Archivo a : response.body()) {
                            JSONObject archObj = new JSONObject();
                            archObj.put("id", a.id);
                            archObj.put("nombre", a.nombre);
                            archObj.put("descripcion", a.descripcion);
                            archObj.put("categoria", a.categoria);
                            resultados.put(archObj);
                        }
                        responseBody.put("resultados", resultados);
                    } else {
                        responseBody.put("error", "No se encontraron resultados");
                    }
                    functionResponse.put("response", responseBody);
                    
                    // Segundo round-trip a Gemini
                    hacerPeticionGemini(historial, nuevoMensaje, previousFunctionCall, functionResponse, callback);
                } catch (JSONException e) {
                    enviarError(callback, "Error procesando búsqueda local.");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<Archivo>> call, Throwable t) {
                enviarError(callback, "Error buscando en base de datos.");
            }
        });
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
