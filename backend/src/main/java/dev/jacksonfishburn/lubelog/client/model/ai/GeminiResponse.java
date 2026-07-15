package dev.jacksonfishburn.lubelog.client.model.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.stream.Stream;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeminiResponse(
        String id,
        String status,
        List<Step> steps
) {
    public String outputText() {
        if (steps == null) {
            return "";
        }
        return steps.stream()
                .filter(step -> "model_output".equals(step.type()))
                .flatMap(step -> step.content() == null ? Stream.empty() : step.content().stream())
                .map(ContentItem::text)
                .filter(text -> text != null && !text.isBlank())
                .reduce("", String::concat);
    }
}
