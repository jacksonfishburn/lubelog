package dev.jacksonfishburn.lubelog.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.jacksonfishburn.lubelog.dto.servicelog.LogResponse;
import dev.jacksonfishburn.lubelog.dto.reminder.ServiceReminderEmailRequest;
import dev.jacksonfishburn.lubelog.entity.ServiceReminder;
import dev.jacksonfishburn.lubelog.entity.ServiceType;
import dev.jacksonfishburn.lubelog.entity.User;
import dev.jacksonfishburn.lubelog.entity.Vehicle;
import dev.jacksonfishburn.lubelog.entity.VehicleService;
import dev.jacksonfishburn.lubelog.repository.ServiceReminderRepository;
import dev.jacksonfishburn.lubelog.service.ReminderEmailService;
import dev.jacksonfishburn.lubelog.service.ReminderService;
import dev.jacksonfishburn.lubelog.service.ServiceLogService;
import dev.jacksonfishburn.lubelog.service.VehicleServiceService;
import jakarta.mail.MessagingException;

@ExtendWith(MockitoExtension.class)
class ReminderServiceTest {

    private static final int DUE_MILEAGE = 40000;

    @Mock
    private ServiceReminderRepository serviceReminderRepository;

    @Mock
    private ServiceLogService serviceLogService;

    @Mock
    private VehicleServiceService vehicleServiceService;

    @Mock
    private ReminderEmailService reminderEmailService;

    @InjectMocks
    private ReminderService reminderService;

    @Test
    void sendDueReminders_skips_whenNoLogHistory() throws Exception {
        VehicleService vehicleService = vehicleService(30000);
        when(vehicleServiceService.findServicesToRemind()).thenReturn(List.of(vehicleService));
        when(serviceLogService.getMostRecentLog(vehicleService)).thenReturn(null);

        reminderService.sendDueReminders();

        verify(reminderEmailService, never()).sendServiceReminder(any());
        verify(serviceReminderRepository, never()).save(any());
    }

    @Test
    void sendDueReminders_skips_whenNotDue() throws Exception {
        VehicleService vehicleService = vehicleService(30000);
        LocalDate today = LocalDate.now();
        stubService(vehicleService,
                logResponse(vehicleService.getId(), DUE_MILEAGE, today.plusDays(60)));
        when(serviceReminderRepository.findByVehicleServiceId(vehicleService.getId()))
                .thenReturn(Optional.empty());

        reminderService.sendDueReminders();

        verify(reminderEmailService, never()).sendServiceReminder(any());
        verify(serviceReminderRepository, never()).save(any());
    }

    @Test
    void sendDueReminders_sendsAndRecordsMileage_whenDueByMileageWithNoPriorReminder() throws Exception {
        VehicleService vehicleService = vehicleService(39600);
        stubService(vehicleService, logResponse(vehicleService.getId(), DUE_MILEAGE, null));
        when(serviceReminderRepository.findByVehicleServiceId(vehicleService.getId()))
                .thenReturn(Optional.empty());

        reminderService.sendDueReminders();

        verify(reminderEmailService).sendServiceReminder(any());
        ServiceReminder saved = captureSavedReminder();
        assertThat(saved.getMileageRemindedAt()).isEqualTo(39600);
        assertThat(saved.getDateRemindedAt()).isNull();
        assertThat(saved.getVehicleService()).isEqualTo(vehicleService);
        assertThat(saved.getUser()).isEqualTo(vehicleService.getVehicle().getUser());
        assertThat(saved.getSentAt()).isNotNull();
    }

    @Test
    void sendDueReminders_sendsAndRecordsDate_whenDueByDateWithNoPriorReminder() throws Exception {
        VehicleService vehicleService = vehicleService(30000);
        LocalDate today = LocalDate.now();
        stubService(vehicleService, logResponse(vehicleService.getId(), null, today.plusDays(10)));
        when(serviceReminderRepository.findByVehicleServiceId(vehicleService.getId()))
                .thenReturn(Optional.empty());

        reminderService.sendDueReminders();

        verify(reminderEmailService).sendServiceReminder(any());
        ServiceReminder saved = captureSavedReminder();
        assertThat(saved.getDateRemindedAt()).isEqualTo(today);
        assertThat(saved.getMileageRemindedAt()).isNull();
    }

    @Test
    void sendDueReminders_sendsOnceAndRecordsBoth_whenDueByDateAndMileage() throws Exception {
        VehicleService vehicleService = vehicleService(39600);
        LocalDate today = LocalDate.now();
        stubService(vehicleService, logResponse(vehicleService.getId(), DUE_MILEAGE, today.plusDays(10)));
        when(serviceReminderRepository.findByVehicleServiceId(vehicleService.getId()))
                .thenReturn(Optional.empty());

        reminderService.sendDueReminders();

        ArgumentCaptor<ServiceReminderEmailRequest> emailCaptor =
                ArgumentCaptor.forClass(ServiceReminderEmailRequest.class);
        verify(reminderEmailService).sendServiceReminder(emailCaptor.capture());
        ServiceReminderEmailRequest email = emailCaptor.getValue();
        assertThat(email.toEmail()).isEqualTo("owner@example.com");
        assertThat(email.serviceTypeName()).isEqualTo("Oil Change");
        assertThat(email.currentMileage()).isEqualTo(39600);
        assertThat(email.dueMileage()).isEqualTo(DUE_MILEAGE);
        assertThat(email.dueDate()).isEqualTo(today.plusDays(10));

        ServiceReminder saved = captureSavedReminder();
        assertThat(saved.getDateRemindedAt()).isEqualTo(today);
        assertThat(saved.getMileageRemindedAt()).isEqualTo(39600);
    }

    @Test
    void sendDueReminders_skips_whenAlreadyRemindedByMileageInWindow() throws Exception {
        VehicleService vehicleService = vehicleService(39600);
        stubService(vehicleService, logResponse(vehicleService.getId(), DUE_MILEAGE, null));
        ServiceReminder existing = ServiceReminder.builder()
                .vehicleService(vehicleService)
                .user(vehicleService.getVehicle().getUser())
                .mileageRemindedAt(39600)
                .build();
        when(serviceReminderRepository.findByVehicleServiceId(vehicleService.getId()))
                .thenReturn(Optional.of(existing));

        reminderService.sendDueReminders();

        verify(reminderEmailService, never()).sendServiceReminder(any());
        verify(serviceReminderRepository, never()).save(any());
    }

    @Test
    void sendDueReminders_sendsOverdue_whenPriorReminderWasAlmostDueMileage() throws Exception {
        VehicleService vehicleService = vehicleService(40200);
        stubService(vehicleService, logResponse(vehicleService.getId(), DUE_MILEAGE, null));
        ServiceReminder existing = ServiceReminder.builder()
                .vehicleService(vehicleService)
                .user(vehicleService.getVehicle().getUser())
                .mileageRemindedAt(39700)
                .build();
        when(serviceReminderRepository.findByVehicleServiceId(vehicleService.getId()))
                .thenReturn(Optional.of(existing));

        reminderService.sendDueReminders();

        verify(reminderEmailService).sendServiceReminder(any());
        ServiceReminder saved = captureSavedReminder();
        assertThat(saved.getMileageRemindedAt()).isEqualTo(40200);
    }

    @Test
    void sendDueReminders_doesNotRecord_whenEmailSendFails() throws Exception {
        VehicleService vehicleService = vehicleService(39600);
        stubService(vehicleService, logResponse(vehicleService.getId(), DUE_MILEAGE, null));
        when(serviceReminderRepository.findByVehicleServiceId(vehicleService.getId()))
                .thenReturn(Optional.empty());
        doThrow(new MessagingException("smtp down"))
                .when(reminderEmailService).sendServiceReminder(any());

        reminderService.sendDueReminders();

        verify(serviceReminderRepository, never()).save(any());
    }

    private void stubService(VehicleService vehicleService, LogResponse log) {
        when(vehicleServiceService.findServicesToRemind()).thenReturn(List.of(vehicleService));
        when(serviceLogService.getMostRecentLog(vehicleService)).thenReturn(log);
    }

    private ServiceReminder captureSavedReminder() {
        ArgumentCaptor<ServiceReminder> captor = ArgumentCaptor.forClass(ServiceReminder.class);
        verify(serviceReminderRepository).save(captor.capture());
        return captor.getValue();
    }

    private VehicleService vehicleService(Integer currentMileage) {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("owner@example.com")
                .build();
        Vehicle vehicle = Vehicle.builder()
                .id(UUID.randomUUID())
                .user(user)
                .nickname("Commuter")
                .year((short) 2019)
                .make("Toyota")
                .model("Camry")
                .mileage(currentMileage)
                .build();
        ServiceType serviceType = ServiceType.builder()
                .id(UUID.randomUUID())
                .name("Oil Change")
                .build();
        return VehicleService.builder()
                .id(UUID.randomUUID())
                .vehicle(vehicle)
                .service(serviceType)
                .build();
    }

    private LogResponse logResponse(UUID vehicleServiceId, Integer mileageDue, LocalDate dateDue) {
        return new LogResponse(
                UUID.randomUUID(),
                vehicleServiceId,
                null,
                mileageDue,
                null,
                dateDue,
                null,
                null,
                List.of());
    }
}
