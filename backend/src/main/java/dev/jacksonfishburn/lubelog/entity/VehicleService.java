package dev.jacksonfishburn.lubelog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "vehicle_services",
    uniqueConstraints = @UniqueConstraint(name = "uq_vehicle_services", columnNames = {"vehicle_id", "service_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleService {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceType service;

    @Column(name = "interval_miles")
    private Integer intervalMiles;

    @Column(name = "interval_months")
    private Short intervalMonths;

    @Column(name = "remind_when_due", nullable = false)
    @Builder.Default
    private boolean remindWhenDue = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}

