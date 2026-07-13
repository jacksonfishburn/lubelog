package dev.jacksonfishburn.lubelog.client.model.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Cost(
        String currency,
        @JsonProperty("total_cost") double totalCost
) {}
