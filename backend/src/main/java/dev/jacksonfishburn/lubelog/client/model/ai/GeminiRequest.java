package dev.jacksonfishburn.lubelog.client.model.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record GeminiRequest(
        String model,
        String input,
        @JsonProperty("response_format") String responseFormat
) {}
