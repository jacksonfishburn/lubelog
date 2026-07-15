package dev.jacksonfishburn.lubelog.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jacksonfishburn.lubelog.client.model.ai.ContentItem;
import dev.jacksonfishburn.lubelog.client.model.ai.GeminiResponse;
import dev.jacksonfishburn.lubelog.client.model.ai.Step;
import dev.jacksonfishburn.lubelog.dto.ai.PartsList;
import dev.jacksonfishburn.lubelog.exception.AiFailureException;
import dev.jacksonfishburn.lubelog.service.ai.GeminiJsonParser;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GeminiJsonParserTest {

    private GeminiJsonParser parser;

    @BeforeEach
    void setUp() {
        parser = new GeminiJsonParser(new ObjectMapper());
    }

    @Test
    void parse_deserializesStructuredOutput() {
        GeminiResponse response = new GeminiResponse(
                "id",
                "completed",
                List.of(new Step(
                        "model_output",
                        List.of(new ContentItem(
                                "text",
                                """
                                {"parts":[{"name":"Oil Filter","specification":"OEM","quantity":1}]}
                                """)))));

        PartsList partsList = parser.parse(response, PartsList.class);

        assertThat(partsList.parts()).hasSize(1);
        assertThat(partsList.parts().getFirst().name()).isEqualTo("Oil Filter");
        assertThat(partsList.parts().getFirst().quantity()).isEqualTo(1);
    }

    @Test
    void parse_throwsAiFailure_whenOutputEmpty() {
        GeminiResponse response = new GeminiResponse("id", "completed", List.of());

        assertThatThrownBy(() -> parser.parse(response, PartsList.class))
                .isInstanceOf(AiFailureException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void parse_throwsAiFailure_whenJsonInvalid() {
        GeminiResponse response = new GeminiResponse(
                "id",
                "completed",
                List.of(new Step("model_output", List.of(new ContentItem("text", "not-json")))));

        assertThatThrownBy(() -> parser.parse(response, PartsList.class))
                .isInstanceOf(AiFailureException.class)
                .hasMessageContaining("Failed to parse");
    }
}
