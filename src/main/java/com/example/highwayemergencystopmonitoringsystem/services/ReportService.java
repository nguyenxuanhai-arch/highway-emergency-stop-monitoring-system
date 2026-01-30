package com.example.highwayemergencystopmonitoringsystem.services;

import com.example.highwayemergencystopmonitoringsystem.dtos.IncidentResponse;
import com.example.highwayemergencystopmonitoringsystem.entities.Incident;
import com.example.highwayemergencystopmonitoringsystem.repositories.IncidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ReportService {

    private final IncidentRepository incidentRepository;
    private final IncidentService incidentService;

    /**
     * Get incident statistics report
     * - Total incidents by status
     * - Average resolution time
     * - Incidents by date range
     */
    public Map<String, Object> getIncidentReport() {
        log.info("Generating incident report");

        List<Incident> allIncidents = incidentRepository.findAll();
        List<Incident> resolvedIncidents = incidentRepository.findByStatus(Incident.IncidentStatus.RESOLVED);

        Map<String, Object> report = new HashMap<>();

        // Count by status
        long detectedCount = allIncidents.stream()
                .filter(i -> i.getStatus() == Incident.IncidentStatus.DETECTED)
                .count();
        long confirmedCount = allIncidents.stream()
                .filter(i -> i.getStatus() == Incident.IncidentStatus.CONFIRMED)
                .count();
        long resolvedCount = resolvedIncidents.size();

        report.put("totalIncidents", allIncidents.size());
        report.put("detectedCount", detectedCount);
        report.put("confirmedCount", confirmedCount);
        report.put("resolvedCount", resolvedCount);

        // Average resolution time
        if (!resolvedIncidents.isEmpty()) {
            double avgResolutionTimeMinutes = resolvedIncidents.stream()
                    .mapToLong(i -> ChronoUnit.MINUTES.between(i.getDetectionTime(), i.getResolutionTime()))
                    .average()
                    .orElse(0);
            report.put("averageResolutionTimeMinutes", avgResolutionTimeMinutes);
        }

        // Latest incidents
        List<IncidentResponse> latestIncidents = incidentService.listAllIncidents().stream()
                .limit(10)
                .toList();
        report.put("latestIncidents", latestIncidents);

        return report;
    }

    /**
     * Get incidents resolved within a specific date range
     */
    public List<IncidentResponse> getResolvedIncidentsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting resolved incidents between {} and {}", startDate, endDate);

        List<Incident> incidents = incidentRepository.findByDetectionTimeAfter(startDate).stream()
                .filter(i -> i.getStatus() == Incident.IncidentStatus.RESOLVED)
                .filter(i -> i.getResolutionTime().isBefore(endDate))
                .toList();

        return incidents.stream()
                .map(incidentService::buildResponseFromIncident)
                .toList();
    }

    /**
     * Get detailed incident statistics
     */
    public Map<String, Object> getDetailedStatistics() {
        log.info("Generating detailed statistics");

        List<Incident> allIncidents = incidentRepository.findAll();
        List<Incident> resolvedIncidents = incidentRepository.findByStatus(Incident.IncidentStatus.RESOLVED);

        Map<String, Object> stats = new HashMap<>();

        // Status distribution
        Map<String, Long> statusDistribution = new HashMap<>();
        statusDistribution.put("DETECTED", allIncidents.stream().filter(i -> i.getStatus() == Incident.IncidentStatus.DETECTED).count());
        statusDistribution.put("CONFIRMED", allIncidents.stream().filter(i -> i.getStatus() == Incident.IncidentStatus.CONFIRMED).count());
        statusDistribution.put("RESOLVED", (long) resolvedIncidents.size());

        stats.put("statusDistribution", statusDistribution);

        // Resolution time percentiles
        if (!resolvedIncidents.isEmpty()) {
            List<Long> resolutionTimes = resolvedIncidents.stream()
                    .map(i -> ChronoUnit.MINUTES.between(i.getDetectionTime(), i.getResolutionTime()))
                    .sorted()
                    .toList();

            stats.put("minResolutionTime", resolutionTimes.get(0));
            stats.put("maxResolutionTime", resolutionTimes.get(resolutionTimes.size() - 1));
            stats.put("medianResolutionTime", resolutionTimes.get(resolutionTimes.size() / 2));
        }

        // Total incidents count
        stats.put("totalIncidentsCount", allIncidents.size());

        return stats;
    }
}
