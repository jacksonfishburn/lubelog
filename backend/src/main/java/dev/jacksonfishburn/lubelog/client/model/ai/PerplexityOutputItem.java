package dev.jacksonfishburn.lubelog.client.model.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PerplexityOutputItem(
        String type,
        List<String> queries,
        List<SearchResult> results,
        List<ContentItem> content
) {}
