package dev.jacksonfishburn.lubelog.client;

import dev.jacksonfishburn.lubelog.client.model.NhtsaDecodeResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import dev.jacksonfishburn.lubelog.exception.VinLookupException;

@Component
public class VinClient {

    private static final String DECODE_VIN_URL =
            "https://vpic.nhtsa.dot.gov/api/vehicles/decodevinvalues/{vin}?format=json";

    private final RestClient restClient;

    public VinClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public NhtsaDecodeResponse decodeVin(String vin) {
        try {
            return restClient.get()
                    .uri(DECODE_VIN_URL, vin)
                    .retrieve()
                    .body(NhtsaDecodeResponse.class);
        } catch (RestClientException ex) {
            throw new VinLookupException("Failed to reach VIN decode service for VIN: " + vin);
        }
    }
}