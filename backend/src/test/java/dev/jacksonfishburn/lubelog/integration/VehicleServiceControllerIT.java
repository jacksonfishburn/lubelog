package dev.jacksonfishburn.lubelog.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import dev.jacksonfishburn.lubelog.dto.VehicleServiceRequest;
import dev.jacksonfishburn.lubelog.entity.ServiceType;
import dev.jacksonfishburn.lubelog.entity.User;
import dev.jacksonfishburn.lubelog.entity.Vehicle;
import dev.jacksonfishburn.lubelog.entity.VehicleService;
import dev.jacksonfishburn.lubelog.repository.ServiceRepository;
import dev.jacksonfishburn.lubelog.repository.UserRepository;
import dev.jacksonfishburn.lubelog.repository.VehicleRepository;
import dev.jacksonfishburn.lubelog.repository.VehicleServiceRepository;
import dev.jacksonfishburn.lubelog.support.TestSecurityConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, TestSecurityConfig.class})
@TestPropertySource(properties = "app.security.test-override.enabled=true")
@ActiveProfiles("test")
class VehicleServiceControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private VehicleServiceRepository vehicleServiceRepository;

    private User currentUser;
    private Vehicle vehicle;
    private ServiceType globalServiceType;

    @BeforeEach
    void setUp() {
        vehicleServiceRepository.deleteAll();
        vehicleRepository.deleteAll();
        serviceRepository.deleteAll();
        userRepository.deleteAll();

        currentUser = userRepository.save(User.builder()
                .keycloakId(TestSecurityConfig.TEST_USER_SUBJECT)
                .email("current-user@example.com")
                .build());

        vehicle = vehicleRepository.save(Vehicle.builder()
                .user(currentUser)
                .year((short) 2019)
                .make("Toyota")
                .model("Camry")
                .trim("SE")
                .vin("4T1BF1FK5HU123456")
                .nickname("Commuter")
                .mileage(30000)
                .build());

        globalServiceType = serviceRepository.save(ServiceType.builder()
                .name("Oil Change")
                .isGlobal(true)
                .build());
    }

    @AfterEach
    void tearDown() {
        vehicleServiceRepository.deleteAll();
        vehicleRepository.deleteAll();
        serviceRepository.deleteAll();
        userRepository.deleteAll();
    }

    private User saveOtherUser() {
        return userRepository.save(User.builder()
                .keycloakId("22222222-2222-2222-2222-222222222222")
                .email("other-user@example.com")
                .build());
    }

    @Test
    void getVehicleServices_returnsOnlyServicesForThisVehicle() throws Exception {
        vehicleServiceRepository.save(VehicleService.builder()
                .vehicle(vehicle)
                .service(globalServiceType)
                .intervalMiles(5000)
                .build());

        User otherUser = saveOtherUser();
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
        vehicleServiceRepository.save(VehicleService.builder()
                .vehicle(othersVehicle)
                .service(globalServiceType)
                .intervalMiles(7500)
                .build());

        mockMvc.perform(get("/api/vehicles/{vehicleId}/services", vehicle.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].intervalMiles").value(5000))
                .andExpect(jsonPath("$[0].serviceTypeName").value("Oil Change"));
    }

    @Test
    void getVehicleServices_returns403_whenVehicleBelongsToAnotherUser() throws Exception {
        User otherUser = saveOtherUser();
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

        mockMvc.perform(get("/api/vehicles/{vehicleId}/services", othersVehicle.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void getVehicleServices_returns404_whenVehicleDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/vehicles/{vehicleId}/services", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void createVehicleService_returns201AndPersistsVehicleService() throws Exception {
        VehicleServiceRequest request = new VehicleServiceRequest(globalServiceType.getId(), 5000, null);

        mockMvc.perform(post("/api/vehicles/{vehicleId}/services", vehicle.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.vehicleId").value(vehicle.getId().toString()))
                .andExpect(jsonPath("$.serviceTypeId").value(globalServiceType.getId().toString()))
                .andExpect(jsonPath("$.serviceTypeName").value("Oil Change"))
                .andExpect(jsonPath("$.intervalMiles").value(5000))
                .andExpect(jsonPath("$.intervalMonths").doesNotExist());
    }

    @Test
    void createVehicleService_returns400_whenBothIntervalsAreNull() throws Exception {
        VehicleServiceRequest request = new VehicleServiceRequest(globalServiceType.getId(), null, null);

        mockMvc.perform(post("/api/vehicles/{vehicleId}/services", vehicle.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void createVehicleService_returns409_whenServiceTypeAlreadyActivatedOnVehicle() throws Exception {
        vehicleServiceRepository.save(VehicleService.builder()
                .vehicle(vehicle)
                .service(globalServiceType)
                .intervalMiles(5000)
                .build());

        VehicleServiceRequest request = new VehicleServiceRequest(globalServiceType.getId(), 7500, null);

        mockMvc.perform(post("/api/vehicles/{vehicleId}/services", vehicle.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void createVehicleService_returns403_whenServiceTypeBelongsToAnotherUser() throws Exception {
        User otherUser = saveOtherUser();
        ServiceType othersServiceType = serviceRepository.save(ServiceType.builder()
                .user(otherUser)
                .name("Other User's Type")
                .isGlobal(false)
                .build());

        VehicleServiceRequest request = new VehicleServiceRequest(othersServiceType.getId(), 5000, null);

        mockMvc.perform(post("/api/vehicles/{vehicleId}/services", vehicle.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createVehicleService_returns404_whenVehicleDoesNotExist() throws Exception {
        VehicleServiceRequest request = new VehicleServiceRequest(globalServiceType.getId(), 5000, null);

        mockMvc.perform(post("/api/vehicles/{vehicleId}/services", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createVehicleService_returns403_whenVehicleBelongsToAnotherUser() throws Exception {
        User otherUser = saveOtherUser();
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

        VehicleServiceRequest request = new VehicleServiceRequest(globalServiceType.getId(), 5000, null);

        mockMvc.perform(post("/api/vehicles/{vehicleId}/services", othersVehicle.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getVehicleService_returnsVehicleService_whenOwner() throws Exception {
        VehicleService vehicleService = vehicleServiceRepository.save(VehicleService.builder()
                .vehicle(vehicle)
                .service(globalServiceType)
                .intervalMonths((short) 6)
                .build());

        mockMvc.perform(get("/api/vehicles/{vehicleId}/services/{vsId}", vehicle.getId(), vehicleService.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vehicleService.getId().toString()))
                .andExpect(jsonPath("$.intervalMonths").value(6));
    }

    @Test
    void getVehicleService_returns404_whenVehicleServiceDoesNotBelongToVehicle() throws Exception {
        User otherUser = saveOtherUser();
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
        VehicleService othersVehicleService = vehicleServiceRepository.save(VehicleService.builder()
                .vehicle(othersVehicle)
                .service(globalServiceType)
                .intervalMiles(5000)
                .build());

        mockMvc.perform(get("/api/vehicles/{vehicleId}/services/{vsId}", vehicle.getId(), othersVehicleService.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getVehicleService_returns403_whenVehicleBelongsToAnotherUser() throws Exception {
        User otherUser = saveOtherUser();
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
        VehicleService othersVehicleService = vehicleServiceRepository.save(VehicleService.builder()
                .vehicle(othersVehicle)
                .service(globalServiceType)
                .intervalMiles(5000)
                .build());

        mockMvc.perform(get("/api/vehicles/{vehicleId}/services/{vsId}", othersVehicle.getId(), othersVehicleService.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateVehicleService_returns200AndUpdatesIntervals_whenOwner() throws Exception {
        VehicleService vehicleService = vehicleServiceRepository.save(VehicleService.builder()
                .vehicle(vehicle)
                .service(globalServiceType)
                .intervalMiles(5000)
                .build());

        VehicleServiceRequest request = new VehicleServiceRequest(globalServiceType.getId(), 7500, 6);

        mockMvc.perform(put("/api/vehicles/{vehicleId}/services/{vsId}", vehicle.getId(), vehicleService.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intervalMiles").value(7500))
                .andExpect(jsonPath("$.intervalMonths").value(6));
    }

    @Test
    void updateVehicleService_returns400_whenBothIntervalsAreNull() throws Exception {
        VehicleService vehicleService = vehicleServiceRepository.save(VehicleService.builder()
                .vehicle(vehicle)
                .service(globalServiceType)
                .intervalMiles(5000)
                .build());

        VehicleServiceRequest request = new VehicleServiceRequest(globalServiceType.getId(), null, null);

        mockMvc.perform(put("/api/vehicles/{vehicleId}/services/{vsId}", vehicle.getId(), vehicleService.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateVehicleService_returns403_whenVehicleBelongsToAnotherUser() throws Exception {
        User otherUser = saveOtherUser();
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
        VehicleService othersVehicleService = vehicleServiceRepository.save(VehicleService.builder()
                .vehicle(othersVehicle)
                .service(globalServiceType)
                .intervalMiles(5000)
                .build());

        VehicleServiceRequest request = new VehicleServiceRequest(globalServiceType.getId(), 7500, null);

        mockMvc.perform(put("/api/vehicles/{vehicleId}/services/{vsId}", othersVehicle.getId(), othersVehicleService.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteVehicleService_returns204AndRemovesVehicleService_whenOwner() throws Exception {
        VehicleService vehicleService = vehicleServiceRepository.save(VehicleService.builder()
                .vehicle(vehicle)
                .service(globalServiceType)
                .intervalMiles(5000)
                .build());

        mockMvc.perform(delete("/api/vehicles/{vehicleId}/services/{vsId}", vehicle.getId(), vehicleService.getId()))
                .andExpect(status().isNoContent());

        assertThat(vehicleServiceRepository.findById(vehicleService.getId())).isEmpty();
    }

    @Test
    void deleteVehicleService_returns403_whenVehicleBelongsToAnotherUser() throws Exception {
        User otherUser = saveOtherUser();
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
        VehicleService othersVehicleService = vehicleServiceRepository.save(VehicleService.builder()
                .vehicle(othersVehicle)
                .service(globalServiceType)
                .intervalMiles(5000)
                .build());

        mockMvc.perform(delete("/api/vehicles/{vehicleId}/services/{vsId}", othersVehicle.getId(), othersVehicleService.getId()))
                .andExpect(status().isForbidden());

        assertThat(vehicleServiceRepository.findById(othersVehicleService.getId())).isPresent();
    }

    @Test
    void deleteVehicleService_returns404_whenVehicleServiceDoesNotExist() throws Exception {
        mockMvc.perform(delete("/api/vehicles/{vehicleId}/services/{vsId}", vehicle.getId(), UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
