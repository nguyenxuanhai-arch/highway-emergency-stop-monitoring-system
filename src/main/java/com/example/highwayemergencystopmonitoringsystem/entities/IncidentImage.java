package com.example.highwayemergencystopmonitoringsystem.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "incident_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false, updatable = false)
    private LocalDateTime capturedAt;
}
