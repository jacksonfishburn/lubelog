package dev.jacksonfishburn.lubelog.service;

import com.fasterxml.jackson.databind.JsonNode;
import dev.jacksonfishburn.lubelog.client.GeminiClient;
import dev.jacksonfishburn.lubelog.client.PerplexityClient;
import dev.jacksonfishburn.lubelog.client.model.ai.GeminiRequest;
import dev.jacksonfishburn.lubelog.client.model.ai.GeminiResponse;
import dev.jacksonfishburn.lubelog.client.model.ai.PerplexitySearchRequest;
import dev.jacksonfishburn.lubelog.client.model.ai.PerplexitySearchResponse;
import dev.jacksonfishburn.lubelog.dto.ai.AiFindPartsResponse;
import dev.jacksonfishburn.lubelog.dto.ai.PartsList;
import dev.jacksonfishburn.lubelog.dto.ai.SelectedParts;
import dev.jacksonfishburn.lubelog.dto.vehicle.VehicleResponse;
import dev.jacksonfishburn.lubelog.entity.ServiceType;
import dev.jacksonfishburn.lubelog.entity.User;
import dev.jacksonfishburn.lubelog.exception.AiFailureException;
import dev.jacksonfishburn.lubelog.service.ai.AiSchemaLoader;
import dev.jacksonfishburn.lubelog.service.ai.FindPartsPromptBuilder;
import dev.jacksonfishburn.lubelog.service.ai.FindPartsResponseAssembler;
import dev.jacksonfishburn.lubelog.service.ai.GeminiJsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiService {

    private static final String FIND_PARTS_SCHEMA_LOCATION = "ai/find-parts-list.schema.json";
    private static final String SELECT_PARTS_SCHEMA_LOCATION = "ai/select-best-parts.schema.json";
    private static final String GEMINI_MODEL = "gemini-3.5-flash";

    private final VehicleService vehicleService;
    private final ServiceTypeService serviceTypeService;

    private final GeminiClient geminiClient;
    private final PerplexityClient perplexityClient;

    private final AiSchemaLoader schemaLoader;
    private final FindPartsPromptBuilder promptBuilder;
    private final GeminiJsonParser geminiJsonParser;
    private final FindPartsResponseAssembler responseAssembler;

    public AiFindPartsResponse findParts(User user, UUID vehicleId, UUID serviceTypeId) {
        VehicleResponse vehicle = vehicleService.getVehicle(user, vehicleId);
        ServiceType serviceType = serviceTypeService.getServiceType(user, serviceTypeId);

        PartsList partsList = findRequiredParts(vehicle, serviceType.getName());
        PerplexitySearchResponse searchResults = searchPartLinks(vehicle, partsList);
        SelectedParts selectedParts = selectBestParts(vehicle, partsList, searchResults);

        return responseAssembler.assemble(selectedParts, searchResults);
    }

    private PartsList findRequiredParts(VehicleResponse vehicle, String serviceTypeName) {
        String prompt = promptBuilder.buildPartsListPrompt(vehicle, serviceTypeName);
        JsonNode schema = schemaLoader.load(FIND_PARTS_SCHEMA_LOCATION);
        PartsList partsList = geminiJsonParser.parse(callGemini(prompt, schema), PartsList.class);

        if (partsList.parts() == null || partsList.parts().isEmpty()) {
            throw new AiFailureException("Gemini returned no required parts");
        }
        return partsList;
    }

    private PerplexitySearchResponse searchPartLinks(VehicleResponse vehicle, PartsList partsList) {
        String prompt = promptBuilder.buildSearchPrompt(vehicle, partsList);
        PerplexitySearchResponse searchResults = searchParts(prompt);

        if (searchResults.allResults().isEmpty()) {
            throw new AiFailureException("Perplexity returned no purchasable part results");
        }
        return searchResults;
    }

    private SelectedParts selectBestParts(
            VehicleResponse vehicle, PartsList partsList, PerplexitySearchResponse searchResults) {
        String prompt = promptBuilder.buildSelectBestPrompt(vehicle, partsList, searchResults);
        JsonNode schema = schemaLoader.load(SELECT_PARTS_SCHEMA_LOCATION);
        return geminiJsonParser.parse(callGemini(prompt, schema), SelectedParts.class);
    }

    private GeminiResponse callGemini(String input, JsonNode jsonSchema) {
        return geminiClient.generate(new GeminiRequest(GEMINI_MODEL, input, jsonSchema));
    }

    private PerplexitySearchResponse searchParts(String input) {
        String instructions = "Return links to real, purchasable products from reputable car part retailers. "
                + "Ensure the products are compatible with the specified vehicle and service type. "
                + "Avoid generic or unrelated products.";
        return perplexityClient.search(new PerplexitySearchRequest(input, false, "pro-search", instructions));
    }
}
