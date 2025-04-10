package com.hetlesaetherta.gui_javafx;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class Api {
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private static final String BASE_URL = "http://127.0.0.1:8080/entities/";

    public Api() {}

    public JsonObject create(String uuid, JsonObject data) {
        return sendRequest(
                HttpRequest.newBuilder()
                        .uri(buildUri(uuid))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(data)))
                        .build(),
                "create"
        );
    }

    public JsonObject fetchAll()  {
        return sendRequest(
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL.substring(0, BASE_URL.length() - 1))) // remove last slash
                        .GET()
                        .build(),
                "fetchAll"
        );
    }

    public JsonObject fetch(String uuid) {
        return sendRequest(
                HttpRequest.newBuilder()
                        .uri(buildUri(uuid))
                        .GET()
                        .build(),
                "fetch"
        );
    }

    public boolean delete(String uuid, boolean recursive) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(BASE_URL + uuid + "?recursive=" + recursive))
                    .DELETE()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public JsonObject update(String uuid, JsonObject data) {
        return sendRequest(
                HttpRequest.newBuilder()
                        .uri(buildUri(uuid))
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(data)))
                        .build(),
                "update"
        );
    }

    public HashMap<String, String> jsonObjectToHashMap(JsonObject jsonObject) {
        HashMap<String, String> map = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            map.put(entry.getKey(), entry.getValue().getAsString());
        }
        return map;
    }

    // --- Helper methods below ---

    private JsonObject sendRequest(HttpRequest request, String action) {
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();

            if (response.statusCode() != 200 || body == null || body.isEmpty()) {
                System.out.println("Unexpected " + action + "() response: " + body);
                return null;
            }

            JsonReader reader = new JsonReader(new StringReader(body));
            reader.setLenient(true); // allow loose/malformed JSON

            JsonElement element = gson.fromJson(reader, JsonElement.class);

            if (element != null && element.isJsonObject()) {
                return element.getAsJsonObject();
            } else {
                System.out.println("Unexpected " + action + "() non-object: " + body);
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed during " + action + "()");
            return null;
        }
    }    private URI buildUri(String uuid) {
        return URI.create(BASE_URL + uuid);
    }
}
