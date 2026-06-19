package dev.jacksonfishburn.lubelog.dto;

import java.time.Instant;
import java.util.UUID;

public record ServiceTypeResponse(
        UUID id,
        String name,
        boolean isGlobal,
        Instant createdAt) {
}
