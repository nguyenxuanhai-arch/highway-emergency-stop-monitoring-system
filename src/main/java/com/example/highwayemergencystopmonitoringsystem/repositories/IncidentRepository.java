package com.example.highwayemergencystopmonitoringsystem.repositories;

import com.example.highwayemergencystopmonitoringsystem.entities.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findByStatus(Incident.IncidentStatus status);

    List<Incident> findByDetectionTimeAfter(LocalDateTime dateTime);

    List<Incident> findByStatusOrderByDetectionTimeDesc(Incident.IncidentStatus status);
    
    /**
     * Count incidents detected between two dates
     */
    long countByDetectionTimeBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find resolved incidents with resolution time for calculating average processing time
     */
    @Query("SELECT i FROM Incident i WHERE i.status = 'RESOLVED' AND i.resolutionTime IS NOT NULL")
    List<Incident> findResolvedIncidentsWithResolutionTime();
    
    /**
     * Count incidents by status and date range
     */
    long countByStatusAndDetectionTimeBetween(Incident.IncidentStatus status, LocalDateTime startDate, LocalDateTime endDate);
}
