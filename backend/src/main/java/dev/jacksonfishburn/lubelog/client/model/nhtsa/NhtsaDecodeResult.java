package dev.jacksonfishburn.lubelog.client.model.nhtsa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NhtsaDecodeResult(
        @JsonProperty("Make") String make,
        @JsonProperty("Model") String model,
        @JsonProperty("ModelYear") String modelYear,
        @JsonProperty("Trim") String trim
) {}
