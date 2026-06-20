package dev.jacksonfishburn.lubelog.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
import dev.jacksonfishburn.lubelog.dto.LogDetailRequest;
import dev.jacksonfishburn.lubelog.dto.LogRequest;
import dev.jacksonfishburn.lubelog.entity.ServiceLog;
import dev.jacksonfishburn.lubelog.entity.ServiceLogDetail;
import dev.jacksonfishburn.lubelog.entity.ServiceType;
import dev.jacksonfishburn.lubelog.entity.User;
import dev.jacksonfishburn.lubelog.entity.Vehicle;
import dev.jacksonfishburn.lubelog.entity.VehicleService;
import dev.jacksonfishburn.lubelog.repository.ServiceLogDetailRepository;
import dev.jacksonfishburn.lubelog.repository.ServiceLogRepository;
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
class ServiceLogControllerIT {

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

    @Autowired
    private ServiceLogRepository serviceLogRepository;

    @Autowired
    private ServiceLogDetailRepository serviceLogDetailRepository;

    private User currentUser;
    private Vehicle vehicle;
    private ServiceType globalServiceType;
    private VehicleService vehicleService;

    @BeforeEach
    void setUp() {
        serviceLogDetailRepository.deleteAll();
        serviceLogRepository.deleteAll();
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

        vehicleService = vehicleServiceRepository.save(VehicleService.builder()
                .vehicle(vehicle)
                .service(globalServiceType)
                .intervalMiles(5000)
                .intervalMonths((short) 6)
                .build());
    }

    @AfterEach
    void tearDown() {
        serviceLogDetailRepository.deleteAll();
        serviceLogRepository.deleteAll();
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

    private VehicleService saveOthersVehicleService() {
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
        return vehicleServiceRepository.save(VehicleService.builder()
                .vehicle(othersVehicle)
                .service(globalServiceType)
                .intervalMiles(7500)
                .build());
    }

    @Test
    void getLogsByVehicleServiceId_returnsOnlyLogsForThatVehicleService() throws Exception {
        serviceLogRepository.save(ServiceLog.builder()
                .vehicleService(vehicleService)
                .doneAtMileage(30000)
                .doneAtDate(LocalDate.of(2026, 1, 1))
                .build());

        VehicleService othersVehicleService = saveOthersVehicleService();
        serviceLogRepository.save(ServiceLog.builder()
                .vehicleService(othersVehicleService)
                .doneAtMileage(15000)
                .doneAtDate(LocalDate.of(2026, 1, 1))
                .build());

        mockMvc.perform(get("/api/logs").param("vehicleServiceId", vehicleService.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].doneAtMileage").value(30000))
                .andExpect(jsonPath("$[0].mileageDue").value(35000));
    }

    @Test
    void getLogsByVehicleId_returnsAllLogsForVehicle() throws Exception {
        serviceLogRepository.save(ServiceLog.builder()
                .vehicleService(vehicleService)
                .doneAtMileage(30000)
                .doneAtDate(LocalDate.of(2026, 1, 1))
                .build());

        mockMvc.perform(get("/api/logs").param("vehicleId", vehicle.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getLogs_returns400_whenNeitherQueryParamProvided() throws Exception {
        mockMvc.perform(get("/api/logs"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createLog_returns201AndComputesDueValues() throws Exception {
        LogRequest request = new LogRequest(
                vehicleService.getId(), 30000, LocalDate.of(2026, 1, 1), new BigDecimal("49.99"), "Synthetic oil", List.of());

        mockMvc.perform(post("/api/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.vehicleServiceId").value(vehicleService.getId().toString()))
                .andExpect(jsonPath("$.doneAtMileage").value(30000))
                .andExpect(jsonPath("$.mileageDue").value(35000))
                .andExpect(jsonPath("$.doneAtDate").value("2026-01-01"))
                .andExpect(jsonPath("$.dateDue").value("2026-07-01"))
                .andExpect(jsonPath("$.cost").value(49.99));

        assertThat(vehicleRepository.findById(vehicle.getId()).orElseThrow().getMileage()).isEqualTo(30000);
    }

    @Test
    void createLog_updatesVehicleMileage_whenDoneAtMileageIsGreaterThanCurrent() throws Exception {
        LogRequest request = new LogRequest(
                vehicleService.getId(), 35000, LocalDate.of(2026, 1, 1), null, null, List.of());

        mockMvc.perform(post("/api/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        assertThat(vehicleRepository.findById(vehicle.getId()).orElseThrow().getMileage()).isEqualTo(35000);
    }

    @Test
    void createLog_returns400_whenDoneAtMileageIsLessThanCurrentVehicleMileage() throws Exception {
        LogRequest request = new LogRequest(
                vehicleService.getId(), 25000, LocalDate.of(2026, 1, 1), null, null, List.of());

        mockMvc.perform(post("/api/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createLog_returns403_whenVehicleServiceBelongsToAnotherUser() throws Exception {
        VehicleService othersVehicleService = saveOthersVehicleService();
        LogRequest request = new LogRequest(
                othersVehicleService.getId(), 16000, LocalDate.of(2026, 1, 1), null, null, List.of());

        mockMvc.perform(post("/api/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getLog_returnsLog_whenOwner() throws Exception {
        ServiceLog log = serviceLogRepository.save(ServiceLog.builder()
                .vehicleService(vehicleService)
                .doneAtMileage(30000)
                .doneAtDate(LocalDate.of(2026, 1, 1))
                .build());

        mockMvc.perform(get("/api/logs/{logId}", log.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(log.getId().toString()));
    }

    @Test
    void getLog_returns403_whenLogBelongsToAnotherUser() throws Exception {
        VehicleService othersVehicleService = saveOthersVehicleService();
        ServiceLog othersLog = serviceLogRepository.save(ServiceLog.builder()
                .vehicleService(othersVehicleService)
                .doneAtMileage(15000)
                .doneAtDate(LocalDate.of(2026, 1, 1))
                .build());

        mockMvc.perform(get("/api/logs/{logId}", othersLog.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void getLog_returns404_whenLogDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/logs/{logId}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateLog_returns200AndUpdatesFieldsAndMileage_whenOwner() throws Exception {
        ServiceLog log = serviceLogRepository.save(ServiceLog.builder()
                .vehicleService(vehicleService)
                .doneAtMileage(30000)
                .doneAtDate(LocalDate.of(2026, 1, 1))
                .build());

        LogRequest request = new LogRequest(
                vehicleService.getId(), 32000, LocalDate.of(2026, 2, 1), new BigDecimal("59.99"), "Updated", List.of());

        mockMvc.perform(put("/api/logs/{logId}", log.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doneAtMileage").value(32000))
                .andExpect(jsonPath("$.notes").value("Updated"));

        assertThat(vehicleRepository.findById(vehicle.getId()).orElseThrow().getMileage()).isEqualTo(32000);
    }

    @Test
    void updateLog_returns400_whenDoneAtMileageIsLessThanCurrentVehicleMileage() throws Exception {
        ServiceLog log = serviceLogRepository.save(ServiceLog.builder()
                .vehicleService(vehicleService)
                .doneAtMileage(30000)
                .doneAtDate(LocalDate.of(2026, 1, 1))
                .build());

        LogRequest request = new LogRequest(
                vehicleService.getId(), 1000, LocalDate.of(2026, 2, 1), null, null, List.of());

        mockMvc.perform(put("/api/logs/{logId}", log.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateLog_returns403_whenLogBelongsToAnotherUser() throws Exception {
        VehicleService othersVehicleService = saveOthersVehicleService();
        ServiceLog othersLog = serviceLogRepository.save(ServiceLog.builder()
                .vehicleService(othersVehicleService)
                .doneAtMileage(15000)
                .doneAtDate(LocalDate.of(2026, 1, 1))
                .build());

        LogRequest request = new LogRequest(
                othersVehicleService.getId(), 16000, LocalDate.of(2026, 2, 1), null, null, List.of());

        mockMvc.perform(put("/api/logs/{logId}", othersLog.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteLog_returns204AndRemovesLog_whenOwner() throws Exception {
        ServiceLog log = serviceLogRepository.save(ServiceLog.builder()
                .vehicleService(vehicleService)
                .doneAtMileage(30000)
                .doneAtDate(LocalDate.of(2026, 1, 1))
                .build());

        mockMvc.perform(delete("/api/logs/{logId}", log.getId()))
                .andExpect(status().isNoContent());

        assertThat(serviceLogRepository.findById(log.getId())).isEmpty();
    }

    @Test
    void deleteLog_returns403_whenLogBelongsToAnotherUser() throws Exception {
        VehicleService othersVehicleService = saveOthersVehicleService();
        ServiceLog othersLog = serviceLogRepository.save(ServiceLog.builder()
                .vehicleService(othersVehicleService)
                .doneAtMileage(15000)
                .doneAtDate(LocalDate.of(2026, 1, 1))
                .build());

        mockMvc.perform(delete("/api/logs/{logId}", othersLog.getId()))
                .andExpect(status().isForbidden());

        assertThat(serviceLogRepository.findById(othersLog.getId())).isPresent();
    }

    @Test
    void deleteLog_returns404_whenLogDoesNotExist() throws Exception {
        mockMvc.perform(delete("/api/logs/{logId}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void addDetail_returns201AndPersistsDetail_whenOwner() throws Exception {
        ServiceLog log = serviceLogRepository.save(ServiceLog.builder()
                .vehicleService(vehicleService)
                .doneAtMileage(30000)
                .doneAtDate(LocalDate.of(2026, 1, 1))
                .build());

        LogDetailRequest request = new LogDetailRequest("Oil Brand", "Mobil 1");

        mockMvc.perform(post("/api/logs/{logId}/details", log.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.key").value("Oil Brand"))
                .andExpect(jsonPath("$.value").value("Mobil 1"));
    }

    @Test
    void addDetail_returns403_whenLogBelongsToAnotherUser() throws Exception {
        VehicleService othersVehicleService = saveOthersVehicleService();
        ServiceLog othersLog = serviceLogRepository.save(ServiceLog.builder()
                .vehicleService(othersVehicleService)
                .doneAtMileage(15000)
                .doneAtDate(LocalDate.of(2026, 1, 1))
                .build());

        LogDetailRequest request = new LogDetailRequest("Oil Brand", "Mobil 1");

        mockMvc.perform(post("/api/logs/{logId}/details", othersLog.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void addDetail_returns404_whenLogDoesNotExist() throws Exception {
        LogDetailRequest request = new LogDetailRequest("Oil Brand", "Mobil 1");

        mockMvc.perform(post("/api/logs/{logId}/details", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteDetail_returns204AndRemovesDetail_whenOwner() throws Exception {
        ServiceLog log = serviceLogRepository.save(ServiceLog.builder()
                .vehicleService(vehicleService)
                .doneAtMileage(30000)
                .doneAtDate(LocalDate.of(2026, 1, 1))
                .build());
        ServiceLogDetail detail = serviceLogDetailRepository.save(ServiceLogDetail.builder()
                .serviceLog(log)
                .detailKey("Oil Brand")
                .value("Mobil 1")
                .build());

        mockMvc.perform(delete("/api/logs/details/{detailId}", detail.getId()))
                .andExpect(status().isNoContent());

        assertThat(serviceLogDetailRepository.findById(detail.getId())).isEmpty();
    }

    @Test
    void deleteDetail_returns403_whenDetailBelongsToAnotherUser() throws Exception {
        VehicleService othersVehicleService = saveOthersVehicleService();
        ServiceLog othersLog = serviceLogRepository.save(ServiceLog.builder()
                .vehicleService(othersVehicleService)
                .doneAtMileage(15000)
                .doneAtDate(LocalDate.of(2026, 1, 1))
                .build());
        ServiceLogDetail othersDetail = serviceLogDetailRepository.save(ServiceLogDetail.builder()
                .serviceLog(othersLog)
                .detailKey("Oil Brand")
                .value("Castrol")
                .build());

        mockMvc.perform(delete("/api/logs/details/{detailId}", othersDetail.getId()))
                .andExpect(status().isForbidden());

        assertThat(serviceLogDetailRepository.findById(othersDetail.getId())).isPresent();
    }

    @Test
    void deleteDetail_returns404_whenDetailDoesNotExist() throws Exception {
        mockMvc.perform(delete("/api/logs/details/{detailId}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
