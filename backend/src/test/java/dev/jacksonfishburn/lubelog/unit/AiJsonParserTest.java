package dev.jacksonfishburn.lubelog.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jacksonfishburn.lubelog.dto.ai.PartsList;
import dev.jacksonfishburn.lubelog.exception.AiFailureException;
import dev.jacksonfishburn.lubelog.service.ai.AiJsonParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AiJsonParserTest {

    private AiJsonParser parser;

    @BeforeEach
    void setUp() {
        parser = new AiJsonParser(new ObjectMapper());
    }

    @Test
    void parse_deserializesStructuredOutput() {
        String text = """
                {"parts":[{"name":"Oil Filter","specification":"OEM","quantity":1}]}
                """;

        PartsList partsList = parser.parse(text, PartsList.class);

        assertThat(partsList.parts()).hasSize(1);
        assertThat(partsList.parts().getFirst().name()).isEqualTo("Oil Filter");
        assertThat(partsList.parts().getFirst().quantity()).isEqualTo(1);
    }

    @Test
    void parse_throwsAiFailure_whenOutputEmpty() {
        assertThatThrownBy(() -> parser.parse("", PartsList.class))
                .isInstanceOf(AiFailureException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void parse_throwsAiFailure_whenJsonInvalid() {
        assertThatThrownBy(() -> parser.parse("not-json", PartsList.class))
                .isInstanceOf(AiFailureException.class)
                .hasMessageContaining("Failed to parse");
    }
}
