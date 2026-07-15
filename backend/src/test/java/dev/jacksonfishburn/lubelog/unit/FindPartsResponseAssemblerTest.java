package dev.jacksonfishburn.lubelog.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.jacksonfishburn.lubelog.client.model.ai.PerplexitySearchResponse;
import dev.jacksonfishburn.lubelog.client.model.ai.PerplexityOutputItem;
import dev.jacksonfishburn.lubelog.client.model.ai.SearchResult;
import dev.jacksonfishburn.lubelog.dto.ai.AiFindPartsResponse;
import dev.jacksonfishburn.lubelog.dto.ai.SelectedParts;
import dev.jacksonfishburn.lubelog.exception.AiFailureException;
import dev.jacksonfishburn.lubelog.service.ai.FindPartsResponseAssembler;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FindPartsResponseAssemblerTest {

    private FindPartsResponseAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new FindPartsResponseAssembler();
    }

    @Test
    void assemble_mapsSelectedIdsToServiceParts() {
        SelectedParts selected = new SelectedParts(List.of(
                new SelectedParts.SelectedPart(2, "Best Oil Filter")));
        PerplexitySearchResponse search = searchResponse(
                new SearchResult(1, "Wrong", "https://a", "a", "source", null, null),
                new SearchResult(2, "Oil Filter", "https://b", "fits civic", "source", null, null));

        AiFindPartsResponse response = assembler.assemble(selected, search);

        assertThat(response.parts()).hasSize(1);
        assertThat(response.parts().getFirst().url()).isEqualTo("https://b");
        assertThat(response.parts().getFirst().title()).isEqualTo("Oil Filter");
        assertThat(response.parts().getFirst().description()).isEqualTo("fits civic");
    }

    @Test
    void assemble_throwsAiFailure_whenSelectedIdMissing() {
        SelectedParts selected = new SelectedParts(List.of(
                new SelectedParts.SelectedPart(99, "Missing")));
        PerplexitySearchResponse search = searchResponse(
                new SearchResult(1, "Oil Filter", "https://a", "snippet", "source", null, null));

        assertThatThrownBy(() -> assembler.assemble(selected, search))
                .isInstanceOf(AiFailureException.class)
                .hasMessageContaining("99");
    }

    @Test
    void assemble_throwsAiFailure_whenSelectionEmpty() {
        PerplexitySearchResponse search = searchResponse(
                new SearchResult(1, "Oil Filter", "https://a", "snippet", "source", null, null));

        assertThatThrownBy(() -> assembler.assemble(new SelectedParts(List.of()), search))
                .isInstanceOf(AiFailureException.class)
                .hasMessageContaining("did not select");
    }

    private static PerplexitySearchResponse searchResponse(SearchResult... results) {
        return new PerplexitySearchResponse(
                "id",
                "completed",
                List.of(new PerplexityOutputItem("search_results", null, List.of(results), null)),
                null);
    }
}
