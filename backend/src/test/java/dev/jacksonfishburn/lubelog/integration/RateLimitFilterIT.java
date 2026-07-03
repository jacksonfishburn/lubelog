package dev.jacksonfishburn.lubelog.integration;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import dev.jacksonfishburn.lubelog.TestcontainersConfiguration;
import dev.jacksonfishburn.lubelog.entity.User;
import dev.jacksonfishburn.lubelog.repository.UserRepository;
import dev.jacksonfishburn.lubelog.support.TestSecurityConfig;

/**
 * Verifies the {@link dev.jacksonfishburn.lubelog.security.RateLimitFilter} enforces the per-user
 * token bucket. The global test profile disables rate limiting; this class re-enables it and shrinks
 * the capacity to 5 so a short request loop can exhaust the bucket. The override creates a distinct
 * Spring context (cached separately from other ITs), so the smaller capacity never leaks elsewhere.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, TestSecurityConfig.class})
@TestPropertySource(properties = {
        "app.security.test-override.enabled=true",
        "app.ratelimit.enabled=true",
        "app.ratelimit.capacity=5",
        "app.ratelimit.refill-tokens=5",
        "app.ratelimit.refill-duration=PT1M"
})
@ActiveProfiles("test")
class RateLimitFilterIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.save(User.builder()
                .keycloakId(TestSecurityConfig.TEST_USER_SUBJECT)
                .email("current-user@example.com")
                .build());
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void returns429_afterCapacityExhausted() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/api/vehicles"))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.message").exists());
    }
}
