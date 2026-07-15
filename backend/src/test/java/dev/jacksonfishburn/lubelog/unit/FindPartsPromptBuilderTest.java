package dev.jacksonfishburn.lubelog.unit;

import static org.assertj.core.api.Assertions.assertThat;

import dev.jacksonfishburn.lubelog.client.model.ai.PerplexityOutputItem;
import dev.jacksonfishburn.lubelog.client.model.ai.PerplexitySearchResponse;
import dev.jacksonfishburn.lubelog.client.model.ai.SearchResult;
import dev.jacksonfishburn.lubelog.dto.ai.PartsList;
import dev.jacksonfishburn.lubelog.dto.vehicle.VehicleResponse;
import dev.jacksonfishburn.lubelog.service.ai.FindPartsPromptBuilder;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FindPartsPromptBuilderTest {

    private FindPartsPromptBuilder promptBuilder;
    private VehicleResponse vehicle;

    @BeforeEach
    void setUp() {
        promptBuilder = new FindPartsPromptBuilder();
        vehicle = new VehicleResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                (short) 2015,
                "Honda",
                "Civic",
                "Sport",
                "VIN",
                "daily",
                50000,
                Instant.now());
    }

    @Test
    void buildPartsListPrompt_includesServiceAndVehicle() {
        String prompt = promptBuilder.buildPartsListPrompt(vehicle, "Oil Change");

        assertThat(prompt).contains("Oil Change");
        assertThat(prompt).contains("2015 Honda Civic Sport");
    }

    @Test
    void buildPartsListPrompt_omitsBlankTrim() {
        VehicleResponse noTrim = new VehicleResponse(
                vehicle.id(),
                vehicle.userId(),
                vehicle.year(),
                vehicle.make(),
                vehicle.model(),
                "  ",
                vehicle.vin(),
                vehicle.nickname(),
                vehicle.mileage(),
                vehicle.createdAt());

        String prompt = promptBuilder.buildPartsListPrompt(noTrim, "Oil Change");

        assertThat(prompt).contains("2015 Honda Civic");
        assertThat(prompt).doesNotContain("Civic  ");
    }

    @Test
    void buildSearchPrompt_listsPartsWithSpecs() {
        PartsList parts = new PartsList(List.of(
                new PartsList.Part("Oil Filter", "OEM", 1),
                new PartsList.Part("Engine Oil", "0W-20", 4)));

        String prompt = promptBuilder.buildSearchPrompt(vehicle, parts);

        assertThat(prompt).contains("Oil Filter (qty 1) — OEM");
        assertThat(prompt).contains("Engine Oil (qty 4) — 0W-20");
        assertThat(prompt).contains("2015 Honda Civic Sport");
    }

    @Test
    void buildSelectBestPrompt_includesCandidateIds() {
        PartsList parts = new PartsList(List.of(new PartsList.Part("Oil Filter", null, 1)));
        PerplexitySearchResponse search = new PerplexitySearchResponse(
                "id",
                "completed",
                List.of(new PerplexityOutputItem(
                        "search_results",
                        null,
                        List.of(new SearchResult(
                                7, "Honda Filter", "https://parts.example/7", "fits", "src", null, null)),
                        null)),
                null);

        String prompt = promptBuilder.buildSelectBestPrompt(vehicle, parts, search);

        assertThat(prompt).contains("[7] Honda Filter");
        assertThat(prompt).contains("https://parts.example/7");
        assertThat(prompt).contains("Oil Filter (qty 1)");
    }
}
