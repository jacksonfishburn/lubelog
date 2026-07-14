package dev.jacksonfishburn.lubelog.controller;

import dev.jacksonfishburn.lubelog.dto.ai.AiFindPartsResponse;
import dev.jacksonfishburn.lubelog.entity.User;
import dev.jacksonfishburn.lubelog.security.AuthUtils;
import dev.jacksonfishburn.lubelog.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/ai/")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final AuthUtils authUtils;

    @GetMapping("/find-parts/vehicles/{vehicle-id}/service-types/{service-type-id}")
    public ResponseEntity<AiFindPartsResponse> findParts(
            @PathVariable UUID vehicleId, @PathVariable UUID serviceTypeId) {
        User user = authUtils.getCurrentUser();
        return ResponseEntity.ok(aiService.findParts(user, vehicleId, serviceTypeId));
    }
}
