package dev.jacksonfishburn.lubelog.client.model.nhtsa;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NhtsaDecodeResponse(
        @JsonProperty("Results") List<NhtsaDecodeResult> results
) {}
