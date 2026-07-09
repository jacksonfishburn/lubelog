package dev.jacksonfishburn.lubelog.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import dev.jacksonfishburn.lubelog.dto.reminder.ServiceReminderEmailRequest;
import dev.jacksonfishburn.lubelog.dto.vehicle.VehicleInfo;
import dev.jacksonfishburn.lubelog.service.ReminderEmailService;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

@ExtendWith(MockitoExtension.class)
class ReminderEmailServiceTest {

    private static final String FROM_ADDRESS = "noreply@test.local";
    private static final String TO_ADDRESS = "owner@example.com";

    @Mock
    private JavaMailSender mailSender;

    private ReminderEmailService service;

    @BeforeEach
    void setUp() {
        service = new ReminderEmailService(mailSender);
        ReflectionTestUtils.setField(service, "fromAddress", FROM_ADDRESS);
        when(mailSender.createMimeMessage())
                .thenReturn(new MimeMessage(Session.getInstance(new Properties())));
    }

    @Test
    void sendServiceReminder_setsRecipientFromAndSubject() throws Exception {
        ServiceReminderEmailRequest request = new ServiceReminderEmailRequest(
                TO_ADDRESS,
                "Oil Change",
                new VehicleInfo("Commuter", (short) 2019, "Toyota", "Camry"),
                30000,
                35000,
                null);

        service.sendServiceReminder(request);

        MimeMessage sent = captureSentMessage();
        assertThat(sent.getAllRecipients()[0].toString()).isEqualTo(TO_ADDRESS);
        assertThat(sent.getFrom()[0].toString()).isEqualTo(FROM_ADDRESS);
        assertThat(sent.getSubject()).isEqualTo("LubeLog reminder: Oil Change for Commuter");
    }

    @Test
    void sendServiceReminder_includesMileageBlock_whenDueMileagePresent() throws Exception {
        ServiceReminderEmailRequest request = new ServiceReminderEmailRequest(
                TO_ADDRESS,
                "Oil Change",
                new VehicleInfo("Commuter", (short) 2019, "Toyota", "Camry"),
                30000,
                35000,
                null);

        service.sendServiceReminder(request);

        String body = captureSentMessage().getContent().toString();
        assertThat(body).contains("Current mileage: 30,000 miles");
        assertThat(body).contains("Due at: 35,000 miles");
    }

    @Test
    void sendServiceReminder_omitsCurrentMileage_whenCurrentMileageNull() throws Exception {
        ServiceReminderEmailRequest request = new ServiceReminderEmailRequest(
                TO_ADDRESS,
                "Oil Change",
                new VehicleInfo("Commuter", (short) 2019, "Toyota", "Camry"),
                null,
                35000,
                null);

        service.sendServiceReminder(request);

        String body = captureSentMessage().getContent().toString();
        assertThat(body).doesNotContain("Current mileage:");
        assertThat(body).contains("Due at: 35,000 miles");
    }

    @Test
    void sendServiceReminder_omitsMileageBlock_whenDueMileageNull() throws Exception {
        ServiceReminderEmailRequest request = new ServiceReminderEmailRequest(
                TO_ADDRESS,
                "Brake Inspection",
                new VehicleInfo(null, (short) 2019, "Toyota", "Camry"),
                null,
                null,
                null);

        service.sendServiceReminder(request);

        MimeMessage sent = captureSentMessage();
        assertThat(sent.getSubject())
                .isEqualTo("LubeLog reminder: Brake Inspection for 2019 Toyota Camry");
        String body = sent.getContent().toString();
        assertThat(body).doesNotContain("Current mileage:");
        assertThat(body).doesNotContain("Due at:");
        assertThat(body).doesNotContain("Due by:");
    }

    @Test
    void sendServiceReminder_includesDateBlock_whenDueDatePresent() throws Exception {
        ServiceReminderEmailRequest request = new ServiceReminderEmailRequest(
                TO_ADDRESS,
                "Brake Inspection",
                new VehicleInfo("Commuter", (short) 2019, "Toyota", "Camry"),
                null,
                null,
                LocalDate.of(2026, 7, 2));

        service.sendServiceReminder(request);

        String body = captureSentMessage().getContent().toString();
        assertThat(body).contains("Due by: Jul 2, 2026");
        assertThat(body).doesNotContain("Due at:");
    }

    @Test
    void sendServiceReminder_includesBothBlocks_whenMileageAndDatePresent() throws Exception {
        ServiceReminderEmailRequest request = new ServiceReminderEmailRequest(
                TO_ADDRESS,
                "Oil Change",
                new VehicleInfo("Commuter", (short) 2019, "Toyota", "Camry"),
                30000,
                35000,
                LocalDate.of(2026, 7, 2));

        service.sendServiceReminder(request);

        String body = captureSentMessage().getContent().toString();
        assertThat(body).contains("Due at: 35,000 miles");
        assertThat(body).contains("Due by: Jul 2, 2026");
    }

    @Test
    void sendServiceReminder_fallsBackToYearMakeModel_whenNicknameBlank() throws Exception {
        ServiceReminderEmailRequest request = new ServiceReminderEmailRequest(
                TO_ADDRESS,
                "Tire Rotation",
                new VehicleInfo("   ", (short) 2021, "Ford", "F-150"),
                null,
                null,
                null);

        service.sendServiceReminder(request);

        assertThat(captureSentMessage().getSubject())
                .isEqualTo("LubeLog reminder: Tire Rotation for 2021 Ford F-150");
    }

    @Test
    void sendServiceReminder_fallsBackToGenericLabel_whenNoVehicleDetails() throws Exception {
        ServiceReminderEmailRequest request = new ServiceReminderEmailRequest(
                TO_ADDRESS,
                "Coolant Flush",
                new VehicleInfo(null, null, null, null),
                null,
                null,
                null);

        service.sendServiceReminder(request);

        assertThat(captureSentMessage().getSubject())
                .isEqualTo("LubeLog reminder: Coolant Flush for your vehicle");
    }

    private MimeMessage captureSentMessage() {
        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());
        return captor.getValue();
    }
}
