package dev.jacksonfishburn.lubelog.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
    name = "service_log_details",
    uniqueConstraints = @UniqueConstraint(name = "uq_service_log_details", columnNames = {"service_log_id", "key"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceLogDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_log_id", nullable = false)
    private ServiceLog serviceLog;

    @Column(name = "key", nullable = false, length = 100)
    private String detailKey;

    @Column(name = "value", nullable = false, columnDefinition = "TEXT")
    private String value;
}

