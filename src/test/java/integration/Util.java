package integration;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.fail;

public class Util {

    private static final HttpClient client = HttpClient.newBuilder().build();
    private static final String HIERARCHY_GET = "hierarchy/get_sup";
    private static final String HTTP_LOCALHOST_4567 = "http://localhost:4567";

    static String performGet(String parameters) {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:4567/" + HIERARCHY_GET + parameters))
                .build();
        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            fail(e.getMessage());
        }
        return response.body();
    }

    static String performPost(String path, String body) {
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .uri(URI.create(HTTP_LOCALHOST_4567 + path))
                .setHeader("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            fail(e.getMessage());
        }
        return response.body();
    }
}
