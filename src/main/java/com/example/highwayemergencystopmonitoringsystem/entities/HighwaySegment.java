package com.example.highwayemergencystopmonitoringsystem.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "highway_segments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HighwaySegment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String segmentCode;

    @Column(length = 255)
    private String locationBounds;
}
