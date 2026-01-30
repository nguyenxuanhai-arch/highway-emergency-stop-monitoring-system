package com.example.highwayemergencystopmonitoringsystem.repositories;

import com.example.highwayemergencystopmonitoringsystem.entities.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findByStatus(Incident.IncidentStatus status);

    List<Incident> findByDetectionTimeAfter(LocalDateTime dateTime);

    List<Incident> findByStatusOrderByDetectionTimeDesc(Incident.IncidentStatus status);
}
