package dev.jacksonfishburn.lubelog.client;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import dev.jacksonfishburn.lubelog.dto.VinDecodeResponse;
import dev.jacksonfishburn.lubelog.exception.VinLookupException;

@Component
public class VinClient {

    private static final String DECODE_VIN_URL =
            "https://vpic.nhtsa.dot.gov/api/vehicles/decodevinvalues/{vin}?format=json";

    private final RestClient restClient;

    public VinClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public VinDecodeResponse decodeVin(String vin) {
        NhtsaDecodeResponse response;
        try {
            response = restClient.get()
                    .uri(DECODE_VIN_URL, vin)
                    .retrieve()
                    .body(NhtsaDecodeResponse.class);
        } catch (RestClientException ex) {
            throw new VinLookupException("Failed to reach VIN decode service for VIN: " + vin);
        }

        NhtsaDecodeResult result = firstResult(response);
        if (result == null || isBlank(result.make()) || isBlank(result.model())) {
            throw new VinLookupException("No VIN decode result found for VIN: " + vin);
        }

        return new VinDecodeResponse(parseYear(result.modelYear()), result.make(), result.model(), result.trim());
    }

    private NhtsaDecodeResult firstResult(NhtsaDecodeResponse response) {
        if (response == null || response.results() == null || response.results().isEmpty()) {
            return null;
        }
        return response.results().get(0);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private Short parseYear(String modelYear) {
        if (isBlank(modelYear)) {
            return null;
        }
        try {
            return Short.parseShort(modelYear.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record NhtsaDecodeResponse(@JsonProperty("Results") List<NhtsaDecodeResult> results) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record NhtsaDecodeResult(
            @JsonProperty("Make") String make,
            @JsonProperty("Model") String model,
            @JsonProperty("ModelYear") String modelYear,
            @JsonProperty("Trim") String trim) {
    }
}