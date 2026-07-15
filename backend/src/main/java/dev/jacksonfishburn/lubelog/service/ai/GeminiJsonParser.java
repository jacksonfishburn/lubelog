package dev.jacksonfishburn.lubelog.service.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jacksonfishburn.lubelog.client.model.ai.GeminiResponse;
import dev.jacksonfishburn.lubelog.exception.AiFailureException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GeminiJsonParser {

    private final ObjectMapper objectMapper;

    public <T> T parse(GeminiResponse response, Class<T> type) {
        String text = response.outputText();
        if (text == null || text.isBlank()) {
            throw new AiFailureException("Gemini returned empty structured output");
        }
        try {
            return objectMapper.readValue(text, type);
        } catch (JsonProcessingException e) {
            throw new AiFailureException("Failed to parse Gemini structured output as " + type.getSimpleName());
        }
    }
}
