package dev.jacksonfishburn.lubelog.service;

import dev.jacksonfishburn.lubelog.client.VinClient;
import dev.jacksonfishburn.lubelog.client.model.NhtsaDecodeResult;
import dev.jacksonfishburn.lubelog.dto.vin.VinDecodeResponse;
import dev.jacksonfishburn.lubelog.exception.VinLookupException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import dev.jacksonfishburn.lubelog.client.model.NhtsaDecodeResponse;

@Service
@RequiredArgsConstructor
public class VinService {

    private final VinClient client;

    public VinDecodeResponse decodeVin(String vin) {
        NhtsaDecodeResponse response = client.decodeVin(vin);

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
}
