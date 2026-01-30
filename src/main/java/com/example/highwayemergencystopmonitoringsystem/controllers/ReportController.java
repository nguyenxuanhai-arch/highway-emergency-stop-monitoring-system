package com.example.highwayemergencystopmonitoringsystem.controllers;

import com.example.highwayemergencystopmonitoringsystem.dtos.IncidentResponse;
import com.example.highwayemergencystopmonitoringsystem.services.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportService reportService;

    /**
     * Get incident statistics report
     * GET /api/reports/incidents
     * Response: status counts, average resolution time, latest incidents
     */
    @GetMapping("/incidents")
    public ResponseEntity<Map<String, Object>> getIncidentReport() {
        try {
            Map<String, Object> report = reportService.getIncidentReport();
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            log.error("Error generating incident report", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get detailed statistics
     * GET /api/reports/statistics
     * Response: status distribution, resolution time percentiles
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> stats = reportService.getDetailedStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get resolved incidents by date range
     * GET /api/reports/resolved?startDate=2024-01-01T00:00:00&endDate=2024-01-31T23:59:59
     * Response: list of resolved incidents within date range
     */
    @GetMapping("/resolved")
    public ResponseEntity<List<IncidentResponse>> getResolvedIncidentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            List<IncidentResponse> incidents = reportService.getResolvedIncidentsByDateRange(startDate, endDate);
            return ResponseEntity.ok(incidents);
        } catch (Exception e) {
            log.error("Error getting resolved incidents", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
