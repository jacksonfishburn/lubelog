package dev.jacksonfishburn.lubelog.dto.ai;

import java.util.List;

public record SelectedParts(List<SelectedPart> parts) {

    public record SelectedPart(int id, String title) {}
}
