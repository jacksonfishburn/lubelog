package dev.jacksonfishburn.lubelog.client.model.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.stream.Stream;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PerplexitySearchResponse(
        String id,
        String status,
        List<PerplexityOutputItem> output,
        Usage usage
) {
    public List<SearchResult> allResults() {
        return output.stream()
                .filter(o -> "search_results".equals(o.type()))
                .flatMap(o -> o.results() == null ? Stream.empty() : o.results().stream())
                .toList();
    }

    public List<String> links() {
        return allResults().stream()
                .map(SearchResult::url)
                .toList();
    }

    public String answerText() {
        return output.stream()
                .filter(o -> "message".equals(o.type()))
                .flatMap(o -> o.content() == null ? Stream.empty() : o.content().stream())
                .map(ContentItem::text)
                .reduce("", String::concat);
    }
}
