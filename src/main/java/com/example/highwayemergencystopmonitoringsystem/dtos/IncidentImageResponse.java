package com.example.highwayemergencystopmonitoringsystem.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentImageResponse {
    private Long id;
    private String filePath;
    private LocalDateTime capturedAt;
}
