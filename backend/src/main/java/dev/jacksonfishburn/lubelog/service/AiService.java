package dev.jacksonfishburn.lubelog.service;

import dev.jacksonfishburn.lubelog.client.GeminiClient;
import dev.jacksonfishburn.lubelog.client.PerplexityClient;
import dev.jacksonfishburn.lubelog.dto.ai.AiFindPartsResponse;
import dev.jacksonfishburn.lubelog.entity.User;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiService {

    private final GeminiClient geminiClient;
    private final PerplexityClient perplexityClient;

    public AiFindPartsResponse findParts(User user, UUID vehicleId, UUID serviceTypeId) {
        throw new NotImplementedException("findParts is not implemented yet");
    }
}
