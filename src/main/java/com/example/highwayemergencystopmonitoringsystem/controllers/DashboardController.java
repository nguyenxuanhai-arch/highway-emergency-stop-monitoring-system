package com.example.highwayemergencystopmonitoringsystem.controllers;

import com.example.highwayemergencystopmonitoringsystem.dtos.IncidentResponse;
import com.example.highwayemergencystopmonitoringsystem.entities.Incident;
import com.example.highwayemergencystopmonitoringsystem.services.IncidentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final IncidentService incidentService;

    /**
     * Get dashboard overview:
     * - Active incidents (DETECTED + CONFIRMED)
     * - Resolved incidents summary
     * - Statistics by status
     */
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getDashboardOverview() {
        try {
            List<IncidentResponse> detectedIncidents = incidentService.listIncidentsByStatus(Incident.IncidentStatus.DETECTED);
            List<IncidentResponse> confirmedIncidents = incidentService.listIncidentsByStatus(Incident.IncidentStatus.CONFIRMED);
            List<IncidentResponse> resolvedIncidents = incidentService.listIncidentsByStatus(Incident.IncidentStatus.RESOLVED);

            Map<String, Object> overview = new HashMap<>();
            overview.put("detectedCount", detectedIncidents.size());
            overview.put("confirmedCount", confirmedIncidents.size());
            overview.put("resolvedCount", resolvedIncidents.size());
            overview.put("activeIncidents", detectedIncidents.size() + confirmedIncidents.size());
            overview.put("detectedIncidents", detectedIncidents);
            overview.put("confirmedIncidents", confirmedIncidents);

            return ResponseEntity.ok(overview);
        } catch (Exception e) {
            log.error("Error getting dashboard overview", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get active incidents (DETECTED + CONFIRMED) for real-time map display
     */
    @GetMapping("/active-incidents")
    public ResponseEntity<List<IncidentResponse>> getActiveIncidents() {
        try {
            List<IncidentResponse> detectedIncidents = new java.util.ArrayList<>(incidentService.listIncidentsByStatus(Incident.IncidentStatus.DETECTED));
            List<IncidentResponse> confirmedIncidents = incidentService.listIncidentsByStatus(Incident.IncidentStatus.CONFIRMED);

            detectedIncidents.addAll(confirmedIncidents);
            detectedIncidents.sort((a, b) -> b.getDetectionTime().compareTo(a.getDetectionTime()));

            return ResponseEntity.ok(detectedIncidents);
        } catch (Exception e) {
            log.error("Error getting active incidents", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
