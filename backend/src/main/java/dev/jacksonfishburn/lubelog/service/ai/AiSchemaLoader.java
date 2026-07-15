package dev.jacksonfishburn.lubelog.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jacksonfishburn.lubelog.exception.CannotReadSchemaException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class AiSchemaLoader {

    private final ObjectMapper objectMapper;

    public JsonNode load(String classpathLocation) {
        try (InputStream in = new ClassPathResource(classpathLocation).getInputStream()) {
            return objectMapper.readTree(in);
        } catch (IOException e) {
            throw new CannotReadSchemaException(
                    "Cannot read schema from classpath location: " + classpathLocation);
        }
    }
}
