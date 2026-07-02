package dev.jacksonfishburn.lubelog.service;

import java.time.Instant;
import java.time.LocalDate;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jacksonfishburn.lubelog.dto.LogResponse;
import dev.jacksonfishburn.lubelog.dto.ServiceReminderEmailRequest;
import dev.jacksonfishburn.lubelog.dto.VehicleInfo;
import dev.jacksonfishburn.lubelog.entity.ServiceReminder;
import dev.jacksonfishburn.lubelog.entity.Vehicle;
import dev.jacksonfishburn.lubelog.entity.VehicleService;
import dev.jacksonfishburn.lubelog.repository.ServiceReminderRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderService {

    private static final int DUE_SOON_DAYS = 30;
    private static final int DUE_SOON_MILES = 500;

    private final ServiceReminderRepository serviceReminderRepository;

    private final ServiceLogService serviceLogService;
    private final VehicleServiceService vehicleServiceService;
    private final ReminderEmailService reminderEmailService;

    @Scheduled(cron = "0 0 10 * * *")
    @Transactional
    public void sendDueReminders() {
        LocalDate today = LocalDate.now();

        for (VehicleService vehicleService : vehicleServiceService.findServicesToRemind()) {
            LogResponse latestLog = serviceLogService.getMostRecentLog(vehicleService);
            if (latestLog == null) {
                continue;
            }

            LocalDate dueDate = latestLog.dateDue();
            Integer dueMileage = latestLog.mileageDue();
            Integer currentMileage = vehicleService.getVehicle().getMileage();

            ServiceReminder reminder =
                    serviceReminderRepository.findByVehicleServiceId(vehicleService.getId()).orElse(null);

            boolean remindByDate = dueDate != null
                    && isDueByDate(dueDate, today)
                    && !alreadyRemindedByDate(reminder, dueDate, today);

            boolean remindByMileage = dueMileage != null && currentMileage != null
                    && isDueByMileage(dueMileage, currentMileage)
                    && !alreadyRemindedByMileage(reminder, dueMileage, currentMileage);

            if (!remindByDate && !remindByMileage) {
                continue;
            }

            sendReminderAndRecord(
                    vehicleService,
                    latestLog,
                    reminder,
                    remindByDate ? today : null,
                    remindByMileage ? currentMileage : null);
        }
    }

    private boolean isDueByDate(LocalDate dueDate, LocalDate today) {
        return !today.isBefore(dueDate.minusDays(DUE_SOON_DAYS));
    }

    private boolean isDueByMileage(Integer dueMileage, Integer currentMileage) {
        return currentMileage >= dueMileage - DUE_SOON_MILES;
    }

    private boolean alreadyRemindedByDate(ServiceReminder reminder, LocalDate dueDate, LocalDate today) {
        if (reminder == null || reminder.getDateRemindedAt() == null) {
            return false;
        }
        LocalDate remindedAt = reminder.getDateRemindedAt();
        if (today.isAfter(dueDate)) {
            return remindedAt.isAfter(dueDate);
        }
        return !remindedAt.isBefore(dueDate.minusDays(DUE_SOON_DAYS));
    }

    private boolean alreadyRemindedByMileage(ServiceReminder reminder, Integer dueMileage, Integer currentMileage) {
        if (reminder == null || reminder.getMileageRemindedAt() == null) {
            return false;
        }
        int remindedAt = reminder.getMileageRemindedAt();
        if (currentMileage > dueMileage) {
            return remindedAt > dueMileage;
        }
        return remindedAt >= dueMileage - DUE_SOON_MILES;
    }

    private void sendReminderAndRecord(VehicleService vehicleService,
                                       LogResponse latestLog,
                                       ServiceReminder existing,
                                       LocalDate dateRemindedAt,
                                       Integer mileageRemindedAt) {
        try {
            reminderEmailService.sendServiceReminder(buildEmailRequest(vehicleService, latestLog));
        } catch (MessagingException e) {
            log.warn("Failed to send service reminder for vehicle service {}", vehicleService.getId(), e);
            return;
        }

        ServiceReminder reminder = existing != null
                ? existing
                : ServiceReminder.builder()
                        .vehicleService(vehicleService)
                        .user(vehicleService.getVehicle().getUser())
                        .build();

        if (dateRemindedAt != null) {
            reminder.setDateRemindedAt(dateRemindedAt);
        }
        if (mileageRemindedAt != null) {
            reminder.setMileageRemindedAt(mileageRemindedAt);
        }
        reminder.setSentAt(Instant.now());

        serviceReminderRepository.save(reminder);
    }

    private ServiceReminderEmailRequest buildEmailRequest(VehicleService vehicleService, LogResponse latestLog) {
        Vehicle vehicle = vehicleService.getVehicle();
        VehicleInfo vehicleInfo = new VehicleInfo(
                vehicle.getNickname(),
                vehicle.getYear(),
                vehicle.getMake(),
                vehicle.getModel());

        return new ServiceReminderEmailRequest(
                vehicle.getUser().getEmail(),
                vehicleService.getService().getName(),
                vehicleInfo,
                vehicle.getMileage(),
                latestLog.mileageDue(),
                latestLog.dateDue());
    }
}
