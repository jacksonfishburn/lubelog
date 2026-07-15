package dev.jacksonfishburn.lubelog.client.model.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PerplexityGenerateRequest(
        String input,
        boolean stream,
        String model,
        @JsonProperty("response_format") JsonNode responseFormat
) {}
