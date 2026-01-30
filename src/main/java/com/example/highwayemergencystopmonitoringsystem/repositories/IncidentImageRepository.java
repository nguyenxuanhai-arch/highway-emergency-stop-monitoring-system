package com.example.highwayemergencystopmonitoringsystem.repositories;

import com.example.highwayemergencystopmonitoringsystem.entities.IncidentImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentImageRepository extends JpaRepository<IncidentImage, Long> {
    List<IncidentImage> findByIncidentId(Long incidentId);
}
