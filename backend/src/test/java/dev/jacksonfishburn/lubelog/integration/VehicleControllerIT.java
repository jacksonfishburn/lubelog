package dev.jacksonfishburn.lubelog.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import dev.jacksonfishburn.lubelog.dto.VehicleRequest;
import dev.jacksonfishburn.lubelog.entity.User;
import dev.jacksonfishburn.lubelog.entity.Vehicle;
import dev.jacksonfishburn.lubelog.repository.UserRepository;
import dev.jacksonfishburn.lubelog.repository.VehicleRepository;
import dev.jacksonfishburn.lubelog.support.TestSecurityConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, TestSecurityConfig.class})
@TestPropertySource(properties = "app.security.test-override.enabled=true")
@ActiveProfiles("test")
class VehicleControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = userRepository.save(User.builder()
                .keycloakId(TestSecurityConfig.TEST_USER_SUBJECT)
                .email("current-user@example.com")
                .build());
    }

    @AfterEach
    void tearDown() {
        vehicleRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createVehicle_returns201AndPersistsVehicle() throws Exception {
        VehicleRequest request = new VehicleRequest(
                "1HGCM82633A004352", "Daily Driver", 50000, (short) 2020, "Honda", "Accord", "EX-L");

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userId").value(currentUser.getId().toString()))
                .andExpect(jsonPath("$.make").value("Honda"))
                .andExpect(jsonPath("$.model").value("Accord"))
                .andExpect(jsonPath("$.vin").value("1HGCM82633A004352"));
    }

    @Test
    void getVehicle_returnsVehicle_whenRequestedByOwner() throws Exception {
        Vehicle vehicle = vehicleRepository.save(Vehicle.builder()
                .user(currentUser)
                .year((short) 2019)
                .make("Toyota")
                .model("Camry")
                .trim("SE")
                .vin("4T1BF1FK5HU123456")
                .nickname("Commuter")
                .mileage(30000)
                .build());

        mockMvc.perform(get("/api/vehicles/{id}", vehicle.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vehicle.getId().toString()))
                .andExpect(jsonPath("$.make").value("Toyota"))
                .andExpect(jsonPath("$.model").value("Camry"));
    }

    @Test
    void getVehicle_returns403_whenVehicleBelongsToAnotherUser() throws Exception {
        User otherUser = userRepository.save(User.builder()
                .keycloakId("22222222-2222-2222-2222-222222222222")
                .email("other-user@example.com")
                .build());

        Vehicle othersVehicle = vehicleRepository.save(Vehicle.builder()
                .user(otherUser)
                .year((short) 2021)
                .make("Ford")
                .model("F-150")
                .trim("XLT")
                .vin("1FTFW1ET1MFA12345")
                .nickname("Work Truck")
                .mileage(15000)
                .build());

        mockMvc.perform(get("/api/vehicles/{id}", othersVehicle.getId()))
                .andExpect(status().isForbidden());
    }
}
