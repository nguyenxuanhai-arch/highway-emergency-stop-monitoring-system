package com.example.highwayemergencystopmonitoringsystem.controllers;

import com.example.highwayemergencystopmonitoringsystem.dtos.IncidentResponse;
import com.example.highwayemergencystopmonitoringsystem.entities.Incident;
import com.example.highwayemergencystopmonitoringsystem.repositories.IncidentRepository;
import com.example.highwayemergencystopmonitoringsystem.services.IncidentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final IncidentService incidentService;
    private final IncidentRepository incidentRepository;

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
    
    /**
     * Get quick statistics for dashboard:
     * - Average processing time (for resolved incidents)
     * - Today's incidents count
     * - This week's incidents count
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getQuickStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Today's date range
            LocalDateTime todayStart = LocalDate.now().atStartOfDay();
            LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
            
            // This week's date range (Monday to now)
            LocalDate today = LocalDate.now();
            LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
            LocalDateTime weekStartDateTime = weekStart.atStartOfDay();
            
            // Count today's incidents
            long todayIncidents = incidentRepository.countByDetectionTimeBetween(todayStart, todayEnd);
            stats.put("todayIncidents", todayIncidents);
            
            // Count this week's incidents
            long weekIncidents = incidentRepository.countByDetectionTimeBetween(weekStartDateTime, todayEnd);
            stats.put("weekIncidents", weekIncidents);
            
            // Calculate average processing time for resolved incidents
            List<Incident> resolvedIncidents = incidentRepository.findResolvedIncidentsWithResolutionTime();
            if (!resolvedIncidents.isEmpty()) {
                double avgMinutes = resolvedIncidents.stream()
                        .filter(i -> i.getResolutionTime() != null && i.getDetectionTime() != null)
                        .mapToLong(i -> ChronoUnit.MINUTES.between(i.getDetectionTime(), i.getResolutionTime()))
                        .average()
                        .orElse(0);
                
                // Format as "Xh Ym" or "Ym" if less than 1 hour
                long hours = (long) avgMinutes / 60;
                long minutes = (long) avgMinutes % 60;
                String avgTimeFormatted;
                if (hours > 0) {
                    avgTimeFormatted = String.format("%dh %dm", hours, minutes);
                } else {
                    avgTimeFormatted = String.format("%dm", minutes);
                }
                stats.put("avgProcessingTime", avgTimeFormatted);
                stats.put("avgProcessingTimeMinutes", avgMinutes);
            } else {
                stats.put("avgProcessingTime", "--");
                stats.put("avgProcessingTimeMinutes", 0);
            }
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting quick statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
