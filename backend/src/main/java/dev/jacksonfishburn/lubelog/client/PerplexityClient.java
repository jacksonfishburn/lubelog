package dev.jacksonfishburn.lubelog.client;

import dev.jacksonfishburn.lubelog.client.model.ai.PerplexityGenerateRequest;
import dev.jacksonfishburn.lubelog.client.model.ai.PerplexitySearchRequest;
import dev.jacksonfishburn.lubelog.client.model.ai.PerplexitySearchResponse;
import dev.jacksonfishburn.lubelog.exception.AiApiAccessException;
import dev.jacksonfishburn.lubelog.exception.AiFailureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

@Component
public class PerplexityClient {

    private static final String BASE_URL =
            "https://api.perplexity.ai";

    private static final String URI =
            "/v1/responses";

    private final RestClient restClient;

    public PerplexityClient(RestClient.Builder restClientBuilder,
                             @Value("${perplexity.api-key}") String apiKey) {
        this.restClient = restClientBuilder
                .baseUrl(BASE_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }

    public PerplexitySearchResponse search(PerplexitySearchRequest request) {
        return post(request, "search", request.input());
    }

    public PerplexitySearchResponse generate(PerplexityGenerateRequest request) {
        return post(request, "generate", request.input());
    }

    private PerplexitySearchResponse post(Object body, String operation, String input) {
        PerplexitySearchResponse response;
        try {
            response = restClient.post()
                    .uri(URI)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(PerplexitySearchResponse.class);
        } catch (RestClientException ex) {
            throw new AiApiAccessException(
                    "Failed to reach Perplexity API for " + operation + " request: " + input);
        }

        requireNonNull(response, "Perplexity API " + operation + " response is null");
        if (!Objects.equals(response.status(), "completed")) {
            throw new AiFailureException(
                    "Perplexity API " + operation + " request failed with status: " + response.status());
        }
        return response;
    }
}
