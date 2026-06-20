package dev.jacksonfishburn.lubelog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LogDetailRequest(
        @NotBlank @Size(max = 100) String key,
        @NotBlank String value) {
}
