package com.example.highwayemergencystopmonitoringsystem.mappers;

import com.example.highwayemergencystopmonitoringsystem.dtos.IncidentResponse;
import com.example.highwayemergencystopmonitoringsystem.entities.Incident;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IncidentMapper {
    IncidentResponse toResponse(Incident incident);

    @Mapping(target = "images", ignore = true)  // Images handled separately in service
    Incident toEntity(IncidentResponse response);
}
