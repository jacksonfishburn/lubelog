package dev.jacksonfishburn.lubelog.service.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jacksonfishburn.lubelog.exception.AiFailureException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiJsonParser {

    private final ObjectMapper objectMapper;

    public <T> T parse(String text, Class<T> type) {
        if (text == null || text.isBlank()) {
            throw new AiFailureException("Perplexity returned empty structured output");
        }
        try {
            return objectMapper.readValue(text, type);
        } catch (JsonProcessingException e) {
            throw new AiFailureException("Failed to parse Perplexity structured output as " + type.getSimpleName());
        }
    }
}
