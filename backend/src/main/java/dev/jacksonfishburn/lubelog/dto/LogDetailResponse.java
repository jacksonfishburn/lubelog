package dev.jacksonfishburn.lubelog.dto;

import java.util.UUID;

public record LogDetailResponse(
        UUID id,
        String key,
        String value) {
}
