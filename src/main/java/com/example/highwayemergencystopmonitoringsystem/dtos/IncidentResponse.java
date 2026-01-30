package com.example.highwayemergencystopmonitoringsystem.dtos;

import com.example.highwayemergencystopmonitoringsystem.entities.Incident.IncidentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentResponse {
    private Long id;
    private Double latitude;
    private Double longitude;
    private String description;
    private IncidentStatus status;
    private LocalDateTime detectionTime;
    private LocalDateTime resolutionTime;
    private List<IncidentImageResponse> images;
}

