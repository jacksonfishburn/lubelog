package dev.jacksonfishburn.lubelog.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import dev.jacksonfishburn.lubelog.dto.reminder.ServiceReminderEmailRequest;
import dev.jacksonfishburn.lubelog.dto.vehicle.VehicleInfo;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReminderEmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    public void sendServiceReminder(ServiceReminderEmailRequest request) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

        helper.setFrom(fromAddress);
        helper.setTo(request.toEmail());
        helper.setSubject(buildSubject(request));
        helper.setText(buildBody(request), false);

        mailSender.send(message);
    }

    private String buildSubject(ServiceReminderEmailRequest request) {
        return "LubeLog reminder: %s for %s".formatted(
                request.serviceTypeName(),
                formatVehicleLabel(request.vehicleInfo()));
    }

    private String buildBody(ServiceReminderEmailRequest request) {
        StringBuilder body = new StringBuilder();
        body.append("Your ")
                .append(request.serviceTypeName())
                .append(" is due soon for ")
                .append(formatVehicleLabel(request.vehicleInfo()))
                .append(".\n\n");
        body.append("Vehicle: ").append(formatVehicleLabel(request.vehicleInfo())).append('\n');

        if (request.dueMileage() != null) {
            body.append('\n');
            if (request.currentMileage() != null) {
                body.append("Current mileage: ")
                        .append(formatMileage(request.currentMileage()))
                        .append(" miles\n");
            }
            body.append("Due at: ")
                    .append(formatMileage(request.dueMileage()))
                    .append(" miles\n");
        }

        if (request.dueDate() != null) {
            body.append('\n');
            body.append("Due by: ")
                    .append(formatDate(request.dueDate()))
                    .append('\n');
        }

        body.append("\nLog in to LubeLog to record service or update your vehicle.");
        return body.toString();
    }

    private String formatVehicleLabel(VehicleInfo vehicleInfo) {
        if (vehicleInfo.nickname() != null && !vehicleInfo.nickname().isBlank()) {
            return vehicleInfo.nickname();
        }

        StringBuilder label = new StringBuilder();
        if (vehicleInfo.year() != null) {
            label.append(vehicleInfo.year()).append(' ');
        }
        if (vehicleInfo.make() != null && !vehicleInfo.make().isBlank()) {
            label.append(vehicleInfo.make()).append(' ');
        }
        if (vehicleInfo.model() != null && !vehicleInfo.model().isBlank()) {
            label.append(vehicleInfo.model());
        }

        String result = label.toString().trim();
        return result.isEmpty() ? "your vehicle" : result;
    }

    private String formatMileage(int mileage) {
        return "%,d".formatted(mileage);
    }

    private String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH));
    }
}
