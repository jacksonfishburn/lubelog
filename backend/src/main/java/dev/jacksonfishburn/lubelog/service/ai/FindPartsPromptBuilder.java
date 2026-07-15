package dev.jacksonfishburn.lubelog.service.ai;

import dev.jacksonfishburn.lubelog.client.model.ai.PerplexitySearchResponse;
import dev.jacksonfishburn.lubelog.client.model.ai.SearchResult;
import dev.jacksonfishburn.lubelog.dto.ai.PartsList;
import dev.jacksonfishburn.lubelog.dto.vehicle.VehicleResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FindPartsPromptBuilder {

    public String buildPartsListPrompt(VehicleResponse vehicle, String serviceTypeName) {
        return "What parts do I need for a " + serviceTypeName + " service on a " + formatVehicle(vehicle) + "? "
                + "List only the parts required to complete this service. "
                + "Include a specification when relevant (e.g. oil viscosity, filter type). "
                + "Include quantity for each part.";
    }

    public String buildSearchPrompt(VehicleResponse vehicle, PartsList partsList) {
        return "Find real purchasable product links for these parts for a " + formatVehicle(vehicle) + ":\n"
                + formatParts(partsList)
                + "\nReturn links from reputable auto parts retailers. "
                + "Each product must be compatible with this vehicle.";
    }

    public String buildSelectBestPrompt(
            VehicleResponse vehicle, PartsList partsList, PerplexitySearchResponse searchResults) {
        return "Select the best purchasable product for each required part "
                + "for a " + formatVehicle(vehicle) + ".\n\n"
                + "Required parts:\n"
                + formatParts(partsList)
                + "\n\nCandidate products from search (use the id field when selecting):\n"
                + formatSearchResults(searchResults.allResults())
                + "\n\nFor each required part, choose exactly one candidate by id. "
                + "Prefer OEM or well-known brands, correct fitment, and reputable retailers.";
    }

    private String formatVehicle(VehicleResponse vehicle) {
        StringBuilder sb = new StringBuilder();
        sb.append(vehicle.year()).append(' ').append(vehicle.make()).append(' ').append(vehicle.model());
        if (vehicle.trim() != null && !vehicle.trim().isBlank()) {
            sb.append(' ').append(vehicle.trim());
        }
        return sb.toString();
    }

    private String formatParts(PartsList partsList) {
        List<PartsList.Part> parts = partsList.parts();
        if (parts == null || parts.isEmpty()) {
            return "- (none)";
        }
        return parts.stream()
                .map(this::formatPart)
                .collect(Collectors.joining("\n"));
    }

    private String formatPart(PartsList.Part part) {
        StringBuilder line = new StringBuilder("- ")
                .append(part.name())
                .append(" (qty ")
                .append(part.quantity())
                .append(')');
        if (part.specification() != null && !part.specification().isBlank()) {
            line.append(" — ").append(part.specification());
        }
        return line.toString();
    }

    private String formatSearchResults(List<SearchResult> results) {
        if (results == null || results.isEmpty()) {
            return "- (no results)";
        }
        return results.stream()
                .map(result -> {
                    StringBuilder line = new StringBuilder()
                            .append('[')
                            .append(result.id())
                            .append("] ")
                            .append(result.title())
                            .append('\n')
                            .append("    url: ")
                            .append(result.url());
                    if (result.snippet() != null && !result.snippet().isBlank()) {
                        line.append('\n').append("    snippet: ").append(result.snippet());
                    }
                    return line.toString();
                })
                .collect(Collectors.joining("\n"));
    }
}
