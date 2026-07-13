package dev.jacksonfishburn.lubelog.client.model.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PerplexitySearchRequest(
        String input,
        boolean stream,
        String preset,
        String instructions
) {}
