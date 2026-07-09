package dev.jacksonfishburn.lubelog.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.jacksonfishburn.lubelog.TestcontainersConfiguration;
import dev.jacksonfishburn.lubelog.dto.servicetype.ServiceTypeRequest;
import dev.jacksonfishburn.lubelog.entity.ServiceType;
import dev.jacksonfishburn.lubelog.entity.User;
import dev.jacksonfishburn.lubelog.repository.ServiceRepository;
import dev.jacksonfishburn.lubelog.repository.UserRepository;
import dev.jacksonfishburn.lubelog.support.TestSecurityConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, TestSecurityConfig.class})
@TestPropertySource(properties = "app.security.test-override.enabled=true")
@ActiveProfiles("test")
class ServiceTypeControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    private User currentUser;

    @BeforeEach
    void setUp() {
        serviceRepository.deleteAll();
        currentUser = userRepository.save(User.builder()
                .keycloakId(TestSecurityConfig.TEST_USER_SUBJECT)
                .email("current-user@example.com")
                .build());
    }

    @AfterEach
    void tearDown() {
        serviceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void getServiceTypes_returnsGlobalsAndCurrentUsersCustomTypes() throws Exception {
        serviceRepository.save(ServiceType.builder()
                .name("Oil Change")
                .isGlobal(true)
                .build());

        serviceRepository.save(ServiceType.builder()
                .user(currentUser)
                .name("Custom Detailing")
                .isGlobal(false)
                .build());

        User otherUser = userRepository.save(User.builder()
                .keycloakId("22222222-2222-2222-2222-222222222222")
                .email("other-user@example.com")
                .build());
        serviceRepository.save(ServiceType.builder()
                .user(otherUser)
                .name("Other User's Type")
                .isGlobal(false)
                .build());

        mockMvc.perform(get("/api/service-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Oil Change", "Custom Detailing")));
    }

    @Test
    void createServiceType_returns201AndPersistsCustomType() throws Exception {
        ServiceTypeRequest request = new ServiceTypeRequest("Brake Inspection");

        mockMvc.perform(post("/api/service-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Brake Inspection"))
                .andExpect(jsonPath("$.isGlobal").value(false));
    }

    @Test
    void createServiceType_returns409_whenNameMatchesUsersExistingCustomType() throws Exception {
        serviceRepository.save(ServiceType.builder()
                .user(currentUser)
                .name("Brake Inspection")
                .isGlobal(false)
                .build());

        ServiceTypeRequest request = new ServiceTypeRequest("Brake Inspection");

        mockMvc.perform(post("/api/service-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void createServiceType_returns409_whenNameMatchesExistingGlobal() throws Exception {
        serviceRepository.save(ServiceType.builder()
                .name("Oil Change")
                .isGlobal(true)
                .build());

        ServiceTypeRequest request = new ServiceTypeRequest("Oil Change");

        mockMvc.perform(post("/api/service-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteServiceType_returns204AndRemovesType_whenRequestedByOwner() throws Exception {
        ServiceType serviceType = serviceRepository.save(ServiceType.builder()
                .user(currentUser)
                .name("Custom Detailing")
                .isGlobal(false)
                .build());

        mockMvc.perform(delete("/api/service-types/{id}", serviceType.getId()))
                .andExpect(status().isNoContent());

        assertThat(serviceRepository.findById(serviceType.getId())).isEmpty();
    }

    @Test
    void deleteServiceType_returns403_whenTypeIsGlobal() throws Exception {
        ServiceType globalType = serviceRepository.save(ServiceType.builder()
                .name("Oil Change")
                .isGlobal(true)
                .build());

        mockMvc.perform(delete("/api/service-types/{id}", globalType.getId()))
                .andExpect(status().isForbidden());

        assertThat(serviceRepository.findById(globalType.getId())).isPresent();
    }

    @Test
    void deleteServiceType_returns403_whenTypeBelongsToAnotherUser() throws Exception {
        User otherUser = userRepository.save(User.builder()
                .keycloakId("22222222-2222-2222-2222-222222222222")
                .email("other-user@example.com")
                .build());
        ServiceType othersType = serviceRepository.save(ServiceType.builder()
                .user(otherUser)
                .name("Other User's Type")
                .isGlobal(false)
                .build());

        mockMvc.perform(delete("/api/service-types/{id}", othersType.getId()))
                .andExpect(status().isForbidden());

        assertThat(serviceRepository.findById(othersType.getId())).isPresent();
    }

    @Test
    void deleteServiceType_returns404_whenTypeDoesNotExist() throws Exception {
        mockMvc.perform(delete("/api/service-types/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
