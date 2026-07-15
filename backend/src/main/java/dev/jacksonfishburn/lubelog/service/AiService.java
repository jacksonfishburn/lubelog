package dev.jacksonfishburn.lubelog.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jacksonfishburn.lubelog.client.GeminiClient;
import dev.jacksonfishburn.lubelog.client.PerplexityClient;
import dev.jacksonfishburn.lubelog.client.model.ai.GeminiRequest;
import dev.jacksonfishburn.lubelog.client.model.ai.GeminiResponse;
import dev.jacksonfishburn.lubelog.client.model.ai.PerplexitySearchRequest;
import dev.jacksonfishburn.lubelog.client.model.ai.PerplexitySearchResponse;
import dev.jacksonfishburn.lubelog.dto.ai.AiFindPartsResponse;
import dev.jacksonfishburn.lubelog.dto.vehicle.VehicleResponse;
import dev.jacksonfishburn.lubelog.entity.ServiceType;
import dev.jacksonfishburn.lubelog.entity.User;
import dev.jacksonfishburn.lubelog.exception.CannotReadSchemaException;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiService {

    private final ObjectMapper objectMapper;

    private final VehicleService vehicleService;
    private final ServiceTypeService serviceTypeService;

    private final GeminiClient geminiClient;
    private final PerplexityClient perplexityClient;

    private final static String FIND_PARTS_SCHEMA_LOCATION = "ai/find-parts-list.schema.json";
    private final static String SELECT_PARTS_SCHEMA_LOCATION = "ai/select-best-parts.schema.json";

    public AiFindPartsResponse findParts(User user, UUID vehicleId, UUID serviceTypeId) {

        VehicleResponse vehicle = vehicleService.getVehicle(user, vehicleId);
        ServiceType serviceType = serviceTypeService.getServiceType(user, serviceTypeId);

        String getPartNamesInput = "";
        JsonNode find_parts_schema = readSchema(FIND_PARTS_SCHEMA_LOCATION);
        GeminiResponse requiredParts = callGemini(getPartNamesInput, find_parts_schema);

        String getPartLinksInput = "";
        PerplexitySearchResponse searchResults = searchParts(getPartLinksInput);

        String selectBestPartsInput = "";
        GeminiResponse selectedResults = callGemini(selectBestPartsInput, readSchema(SELECT_PARTS_SCHEMA_LOCATION));

        return buildResponse(selectedResults, searchResults);
    }

    private JsonNode readSchema(String classpathLocation) {
        try (InputStream in = new ClassPathResource(classpathLocation).getInputStream()) {
            return objectMapper.readTree(in);
        } catch (IOException e) {
            throw new CannotReadSchemaException("Cannot read schema from classpath location: " + classpathLocation);
        }
    }

    private GeminiResponse callGemini(String input, JsonNode jsonSchema) {
        String geminiModel = "gemini-3.5-flash";

        GeminiRequest partNamesRequest = new GeminiRequest(geminiModel, input, jsonSchema);
        return geminiClient.generate(partNamesRequest);
    }

    private PerplexitySearchResponse searchParts(String input) {
        String preset = "pro-search";
        String instructions = "Return links to real, purchasable products from reputable car part retailers. " +
                "Ensure the products are compatible with the specified vehicle and service type. " +
                "Avoid generic or unrelated products.";
        PerplexitySearchRequest request = new PerplexitySearchRequest(input, false, preset, instructions);

        return perplexityClient.search(request);
    }

    private AiFindPartsResponse buildResponse(GeminiResponse selectedResults, PerplexitySearchResponse searchResults) {
        throw new NotImplementedException("buildResponse is not implemented yet");
    }
}
