package com.example.highwayemergencystopmonitoringsystem.controllers;

import com.example.highwayemergencystopmonitoringsystem.dtos.IncidentRequest;
import com.example.highwayemergencystopmonitoringsystem.dtos.IncidentResponse;
import com.example.highwayemergencystopmonitoringsystem.entities.Incident;
import com.example.highwayemergencystopmonitoringsystem.services.IncidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
@Slf4j
public class IncidentController {

    private final IncidentService incidentService;

    /**
     * UC-01: Create incident with initial image
     * POST /api/incidents
     * Request: multipart/form-data with IncidentRequest (latitude, longitude, description) + image file
     * Response: IncidentResponse with status=DETECTED, detection_time=now()
     */
    @PostMapping
    public ResponseEntity<IncidentResponse> createIncident(
            @Valid @RequestParam("latitude") Double latitude,
            @Valid @RequestParam("longitude") Double longitude,
            @Valid @RequestParam("description") String description,
            @RequestParam("image") MultipartFile imageFile) {

        try {
            IncidentRequest request = IncidentRequest.builder()
                    .latitude(latitude)
                    .longitude(longitude)
                    .description(description)
                    .build();

            IncidentResponse response = incidentService.createIncident(request, imageFile);
            log.info("Incident created with id: {}", response.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IOException e) {
            log.error("Error creating incident", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * UC-01: Get incident details with all images
     * GET /api/incidents/{id}
     * Response: IncidentResponse with all linked images
     */
    @GetMapping("/{id}")
    public ResponseEntity<IncidentResponse> getIncident(@PathVariable Long id) {
        try {
            IncidentResponse response = incidentService.getIncidentById(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Incident not found: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * UC-01: List incidents by status
     * GET /api/incidents?status=DETECTED
     * Response: List of IncidentResponse ordered by detection_time (recent first)
     */
    @GetMapping
    public ResponseEntity<List<IncidentResponse>> listIncidents(
            @RequestParam(required = false) String status) {
        try {
            List<IncidentResponse> incidents;
            if (status != null && !status.isEmpty()) {
                Incident.IncidentStatus incidentStatus = Incident.IncidentStatus.valueOf(status.toUpperCase());
                incidents = incidentService.listIncidentsByStatus(incidentStatus);
            } else {
                incidents = incidentService.listAllIncidents();
            }
            return ResponseEntity.ok(incidents);
        } catch (IllegalArgumentException e) {
            log.error("Invalid status: {}", status);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * UC-01: Confirm incident (operator reviews image and confirms)
     * PUT /api/incidents/{id}/confirm
     * Validation: incident status must be DETECTED, must have at least 1 image
     * Response: IncidentResponse with status=CONFIRMED
     */
    @PutMapping("/{id}/confirm")
    public ResponseEntity<IncidentResponse> confirmIncident(@PathVariable Long id) {
        try {
            IncidentResponse response = incidentService.confirmIncident(id);
            log.info("Incident {} confirmed", id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Error confirming incident: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * UC-01: Resolve incident (end state)
     * PUT /api/incidents/{id}/resolve
     * Sets: status=RESOLVED, resolution_time=now()
     * Response: IncidentResponse with status=RESOLVED, resolution_time set
     */
    @PutMapping("/{id}/resolve")
    public ResponseEntity<IncidentResponse> resolveIncident(@PathVariable Long id) {
        try {
            IncidentResponse response = incidentService.resolveIncident(id);
            log.info("Incident {} resolved", id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Error resolving incident: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * UC-01 A1: Upload additional image to existing incident
     * POST /api/incidents/{id}/images
     * Request: multipart/form-data with image file
     * Validation: incident must exist, status != RESOLVED, image valid
     * Response: IncidentResponse with all images including new one
     */
    @PostMapping("/{id}/images")
    public ResponseEntity<IncidentResponse> addImageToIncident(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile imageFile) {
        try {
            IncidentResponse response = incidentService.addImage(id, imageFile);
            log.info("Image added to incident {}", id);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IOException e) {
            log.error("Error adding image to incident", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * GET image file from storage
     * GET /api/incidents/image/{imagePath}
     * Example: /api/incidents/image/uploads/incidents/uuid_filename.png
     * Returns: Image file with appropriate content type (jpeg, png, gif, webp)
     */
    @GetMapping("/image/**")
    public ResponseEntity<Resource> getImage(
            @RequestParam(value = "path", required = false) String imagePath,
            jakarta.servlet.http.HttpServletRequest request) {
        try {
            // Extract path from URL
            String fullPath = request.getRequestURI();
            String pathFromUrl = fullPath.replace("/api/incidents/image/", "");
            
            // URL decode the path to handle spaces and special characters
            String decodedPath = java.net.URLDecoder.decode(pathFromUrl, java.nio.charset.StandardCharsets.UTF_8);
            
            // Use path parameter if provided, otherwise use URL path
            String finalPath = imagePath != null ? imagePath : decodedPath;
            
            // Normalize both forward and back slashes to forward slashes
            String normalizedPath = finalPath.replace("\\", "/");
            
            // Convert forward slashes back to system-specific for file system access
            Path filePath = Paths.get(normalizedPath.replace("/", java.io.File.separator));
            Path normalizedFilePath = filePath.normalize();
            Path uploadsDir = Paths.get("uploads").normalize();
            
            log.debug("Requested path: {}, Normalized: {}", finalPath, normalizedFilePath);
            
            // Security check: ensure path is within uploads directory
            if (!normalizedFilePath.startsWith(uploadsDir)) {
                log.warn("Access denied to path: {}", normalizedPath);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // Check if file exists
            if (!Files.exists(normalizedFilePath)) {
                log.warn("File not found: {} (looked for: {})", normalizedPath, normalizedFilePath);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            
            // Determine media type
            String contentType = Files.probeContentType(normalizedFilePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            log.info("Serving image: {}", normalizedFilePath);
            Resource resource = new FileSystemResource(normalizedFilePath);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
                    
        } catch (IOException e) {
            log.error("Error retrieving image", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
