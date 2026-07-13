package dev.jacksonfishburn.lubelog.client.model.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SearchResult(
        int id,
        String title,
        String url,
        String snippet,
        String source,
        String date,
        @JsonProperty("last_updated") String lastUpdated
) {}
