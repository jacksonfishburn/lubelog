package dev.jacksonfishburn.lubelog.service.ai;

import dev.jacksonfishburn.lubelog.client.model.ai.PerplexitySearchResponse;
import dev.jacksonfishburn.lubelog.client.model.ai.SearchResult;
import dev.jacksonfishburn.lubelog.dto.ai.AiFindPartsResponse;
import dev.jacksonfishburn.lubelog.dto.ai.SelectedParts;
import dev.jacksonfishburn.lubelog.dto.ai.ServicePart;
import dev.jacksonfishburn.lubelog.exception.AiFailureException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class FindPartsResponseAssembler {

    public AiFindPartsResponse assemble(SelectedParts selectedParts, PerplexitySearchResponse searchResults) {
        if (selectedParts == null || selectedParts.parts() == null || selectedParts.parts().isEmpty()) {
            throw new AiFailureException("Perplexity did not select any parts");
        }

        Map<Integer, SearchResult> resultsById = searchResults.allResults().stream()
                .collect(Collectors.toMap(SearchResult::id, Function.identity(), (left, right) -> left));

        List<ServicePart> parts = new ArrayList<>();
        for (SelectedParts.SelectedPart selected : selectedParts.parts()) {
            SearchResult result = resultsById.get(selected.id());
            if (result == null) {
                throw new AiFailureException(
                        "Selected part id not found in search results: " + selected.id());
            }
            parts.add(new ServicePart(result.url(), result.title(), result.snippet()));
        }

        return new AiFindPartsResponse(parts);
    }
}
