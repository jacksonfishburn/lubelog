package dev.jacksonfishburn.lubelog.client;

import dev.jacksonfishburn.lubelog.client.model.ai.GeminiRequest;
import dev.jacksonfishburn.lubelog.client.model.ai.GeminiResponse;
import dev.jacksonfishburn.lubelog.exception.AiApiAccessException;
import dev.jacksonfishburn.lubelog.exception.AiFailureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

@Component
public class GeminiClient {

    private static final String BASE_URL =
            "https://generativelanguage.googleapis.com";

    private static final String URI =
            "/v1beta/interactions";

    private final RestClient restClient;

    public GeminiClient(RestClient.Builder restClientBuilder,
                        @Value("${gemini.api-key}") String apiKey) {
        this.restClient = restClientBuilder
                .baseUrl(BASE_URL)
                .defaultHeader("x-goog-api-key", apiKey)
                .build();
    }

    public GeminiResponse generate(GeminiRequest request) {
        GeminiResponse response;
        try {
            response = restClient.post()
                    .uri(URI)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(GeminiResponse.class);
        } catch (RestClientException ex) {
            throw new AiApiAccessException("Failed to reach Gemini API for generate request: " + request.input());
        }

        requireNonNull(response, "Gemini API generate response is null");
        if (!Objects.equals(response.status(), "completed")) {
            throw new AiFailureException("Gemini API generate request failed with status: " + response.status());
        }
        return response;
    }
}
