package com.example.highwayemergencystopmonitoringsystem.services;

import com.example.highwayemergencystopmonitoringsystem.dtos.IncidentImageResponse;
import com.example.highwayemergencystopmonitoringsystem.dtos.IncidentRequest;
import com.example.highwayemergencystopmonitoringsystem.dtos.IncidentResponse;
import com.example.highwayemergencystopmonitoringsystem.entities.Incident;
import com.example.highwayemergencystopmonitoringsystem.entities.IncidentImage;
import com.example.highwayemergencystopmonitoringsystem.mappers.IncidentImageMapper;
import com.example.highwayemergencystopmonitoringsystem.mappers.IncidentMapper;
import com.example.highwayemergencystopmonitoringsystem.repositories.IncidentImageRepository;
import com.example.highwayemergencystopmonitoringsystem.repositories.IncidentRepository;
import com.example.highwayemergencystopmonitoringsystem.websocket.IncidentWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final IncidentImageRepository incidentImageRepository;
    private final IncidentMapper incidentMapper;
    private final IncidentImageMapper incidentImageMapper;
    private final IncidentWebSocketHandler webSocketHandler;

    private static final String UPLOAD_DIR = "uploads/incidents";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_MIME_TYPES = {"image/jpeg", "image/png", "image/gif", "image/webp"};

    /**
     * UC-01: Create incident with initial image
     * Status: DETECTED, detection_time: now()
     */
    public IncidentResponse createIncident(IncidentRequest request, MultipartFile imageFile) throws IOException {
        log.info("Creating incident at location ({}, {})", request.getLatitude(), request.getLongitude());

        // Validate image
        validateImage(imageFile);

        // Create incident
        Incident incident = Incident.builder()
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .description(request.getDescription())
                .status(Incident.IncidentStatus.DETECTED)
                .detectionTime(LocalDateTime.now())
                .build();

        incident = incidentRepository.save(incident);

        // Save image
        IncidentImage incidentImage = saveImage(incident, imageFile);

        // Return response with image
        IncidentResponse response = incidentMapper.toResponse(incident);
        response.setImages(List.of(incidentImageMapper.toResponse(incidentImage)));

        log.info("Incident created with id: {}", incident.getId());
        
        // Broadcast incident created event via WebSocket
        webSocketHandler.broadcastIncidentCreated(response);
        
        return response;
    }

    /**
     * UC-01: Confirm incident (operator reviews image)
     * Status: DETECTED -> CONFIRMED
     * Validation: must have at least 1 image
     */
    public IncidentResponse confirmIncident(Long incidentId) {
        log.info("Confirming incident: {}", incidentId);

        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found: " + incidentId));

        // Validate current status
        if (incident.getStatus() != Incident.IncidentStatus.DETECTED) {
            throw new IllegalArgumentException("Incident must be in DETECTED status to confirm");
        }

        // Validate has images
        List<IncidentImage> images = incidentImageRepository.findByIncidentId(incidentId);
        if (images.isEmpty()) {
            throw new IllegalArgumentException("Cannot confirm incident without images");
        }

        incident.setStatus(Incident.IncidentStatus.CONFIRMED);
        incident = incidentRepository.save(incident);

        log.info("Incident {} confirmed", incidentId);
        IncidentResponse response = buildResponse(incident);
        
        // Broadcast incident confirmed event via WebSocket
        webSocketHandler.broadcastIncidentConfirmed(response);
        
        return response;
    }

    /**
     * UC-01: Resolve incident (end state)
     * Status: CONFIRMED -> RESOLVED
     * Sets: resolution_time = now()
     */
    public IncidentResponse resolveIncident(Long incidentId) {
        log.info("Resolving incident: {}", incidentId);

        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found: " + incidentId));

        // Validate current status (allow CONFIRMED or DETECTED for flexibility)
        if (incident.getStatus() == Incident.IncidentStatus.RESOLVED) {
            throw new IllegalArgumentException("Incident is already resolved");
        }

        incident.setStatus(Incident.IncidentStatus.RESOLVED);
        incident.setResolutionTime(LocalDateTime.now());
        incident = incidentRepository.save(incident);

        log.info("Incident {} resolved at {}", incidentId, incident.getResolutionTime());
        IncidentResponse response = buildResponse(incident);
        
        // Broadcast incident resolved event via WebSocket
        webSocketHandler.broadcastIncidentResolved(response);
        
        return response;
    }

    /**
     * Get incident by ID with all images
     */
    public IncidentResponse getIncidentById(Long incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found: " + incidentId));
        return buildResponse(incident);
    }

    /**
     * List all incidents with given status
     */
    public List<IncidentResponse> listIncidentsByStatus(Incident.IncidentStatus status) {
        return incidentRepository.findByStatusOrderByDetectionTimeDesc(status)
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    /**
     * UC-01 A1: Add additional image to existing incident
     * Incident status != RESOLVED
     */
    public IncidentResponse addImage(Long incidentId, MultipartFile imageFile) throws IOException {
        log.info("Adding image to incident: {}", incidentId);

        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found: " + incidentId));

        if (incident.getStatus() == Incident.IncidentStatus.RESOLVED) {
            throw new IllegalArgumentException("Cannot add images to resolved incident");
        }

        // Validate image
        validateImage(imageFile);

        // Save image
        saveImage(incident, imageFile);

        // Reload incident with updated images
        incident = incidentRepository.findById(incidentId).orElseThrow();

        log.info("Image added to incident {}", incidentId);
        IncidentResponse response = buildResponse(incident);
        
        // Broadcast image added event via WebSocket
        webSocketHandler.broadcastImageAdded(incidentId, response);
        
        return response;
    }

    /**
     * Get all incidents (recent first)
     */
    public List<IncidentResponse> listAllIncidents() {
        return incidentRepository.findAll().stream()
                .sorted((a, b) -> b.getDetectionTime().compareTo(a.getDetectionTime()))
                .map(this::buildResponse)
                .toList();
    }

    // ==================== Helper Methods ====================

    private IncidentImage saveImage(Incident incident, MultipartFile file) throws IOException {
        // Remove spaces from original filename
        String originalFilename = file.getOriginalFilename().replaceAll("\\s+", "_");
        String filename = UUID.randomUUID() + "_" + originalFilename;
        Path uploadPath = Paths.get(UPLOAD_DIR);
        Files.createDirectories(uploadPath);

        Path filePath = uploadPath.resolve(filename);
        Files.write(filePath, file.getBytes());

        // Normalize path to use forward slashes for consistency
        String normalizedPath = filePath.toString().replace("\\", "/");

        IncidentImage incidentImage = IncidentImage.builder()
                .incident(incident)
                .filePath(normalizedPath)
                .capturedAt(LocalDateTime.now())
                .build();

        return incidentImageRepository.save(incidentImage);
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Image file size exceeds 5MB limit");
        }

        String mimeType = file.getContentType();
        boolean isValidMimeType = false;
        for (String allowed : ALLOWED_MIME_TYPES) {
            if (allowed.equals(mimeType)) {
                isValidMimeType = true;
                break;
            }
        }

        if (!isValidMimeType) {
            throw new IllegalArgumentException("Only JPEG, PNG, GIF, and WebP images are allowed");
        }
    }

    private IncidentResponse buildResponse(Incident incident) {
        List<IncidentImage> images = incidentImageRepository.findByIncidentId(incident.getId());
        List<IncidentImageResponse> imageResponses = images.stream()
                .map(incidentImageMapper::toResponse)
                .toList();

        IncidentResponse response = incidentMapper.toResponse(incident);
        response.setImages(imageResponses);
        return response;
    }

    /**
     * Public method for external services (e.g., ReportService) to build response
     */
    public IncidentResponse buildResponseFromIncident(Incident incident) {
        return buildResponse(incident);
    }
}
