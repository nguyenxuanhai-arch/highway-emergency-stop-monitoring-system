package com.example.highwayemergencystopmonitoringsystem.mappers;

import com.example.highwayemergencystopmonitoringsystem.dtos.IncidentImageResponse;
import com.example.highwayemergencystopmonitoringsystem.entities.IncidentImage;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IncidentImageMapper {
    IncidentImageResponse toResponse(IncidentImage incidentImage);

    IncidentImage toEntity(IncidentImageResponse response);
}
