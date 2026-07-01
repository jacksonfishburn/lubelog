package dev.jacksonfishburn.lubelog.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "service_reminders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_service_id", nullable = false)
    private VehicleService vehicleService;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "sent_at", nullable = false)
    @Builder.Default
    private Instant sentAt = Instant.now();

    @Column(name = "date_reminded_at")
    private LocalDate dateRemindedAt;

    @Column(name = "mileage_reminded_at")
    private Integer mileageRemindedAt;

    @Column(name = "channel", nullable = false)
    @Builder.Default
    private String channel = "EMAIL";
}
