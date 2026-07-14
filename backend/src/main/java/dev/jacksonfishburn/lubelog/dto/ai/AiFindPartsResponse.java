package dev.jacksonfishburn.lubelog.dto.ai;

import java.util.List;

public record AiFindPartsResponse(
        List<ServicePart> parts
) { }
