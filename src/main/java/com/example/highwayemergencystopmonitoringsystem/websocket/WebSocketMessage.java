package com.example.highwayemergencystopmonitoringsystem.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket message DTO for broadcasting incident updates
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {
    
    @JsonProperty("type")
    private String type; // INCIDENT_CREATED, INCIDENT_CONFIRMED, INCIDENT_RESOLVED, IMAGE_ADDED
    
    @JsonProperty("data")
    private Object data; // IncidentResponse or other relevant data
    
    @JsonProperty("timestamp")
    private Long timestamp;
}
