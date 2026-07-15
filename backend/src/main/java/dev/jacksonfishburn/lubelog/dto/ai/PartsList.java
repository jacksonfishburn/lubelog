package dev.jacksonfishburn.lubelog.dto.ai;

import java.util.List;

public record PartsList(List<Part> parts) {

    public record Part(String name, String specification, int quantity) {}
}
