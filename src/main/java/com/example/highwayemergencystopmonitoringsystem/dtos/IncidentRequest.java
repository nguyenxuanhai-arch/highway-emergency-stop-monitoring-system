package com.example.highwayemergencystopmonitoringsystem.dtos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentRequest {

    @NotNull(message = "Latitude cannot be null")
    @Min(value = -90, message = "Latitude must be >= -90")
    @Max(value = 90, message = "Latitude must be <= 90")
    private Double latitude;

    @NotNull(message = "Longitude cannot be null")
    @Min(value = -180, message = "Longitude must be >= -180")
    @Max(value = 180, message = "Longitude must be <= 180")
    private Double longitude;

    @NotBlank(message = "Description cannot be blank")
    @Size(min = 1, max = 500, message = "Description must be between 1 and 500 characters")
    private String description;
}

