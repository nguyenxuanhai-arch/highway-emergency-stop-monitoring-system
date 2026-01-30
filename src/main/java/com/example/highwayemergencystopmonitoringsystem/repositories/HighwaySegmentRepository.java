package com.example.highwayemergencystopmonitoringsystem.repositories;

import com.example.highwayemergencystopmonitoringsystem.entities.HighwaySegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HighwaySegmentRepository extends JpaRepository<HighwaySegment, Long> {
    Optional<HighwaySegment> findBySegmentCode(String segmentCode);
}
