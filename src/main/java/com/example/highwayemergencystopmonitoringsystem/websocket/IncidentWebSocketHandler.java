package com.example.highwayemergencystopmonitoringsystem.websocket;

import com.example.highwayemergencystopmonitoringsystem.dtos.IncidentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Service to send realtime incident updates via WebSocket
 * Broadcasts incident events to all connected clients
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IncidentWebSocketHandler {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Broadcast incident created event
     */
    public void broadcastIncidentCreated(IncidentResponse incident) {
        try {
            WebSocketMessage message = WebSocketMessage.builder()
                    .type("INCIDENT_CREATED")
                    .data(incident)
                    .timestamp(System.currentTimeMillis())
                    .build();
            messagingTemplate.convertAndSend("/topic/incidents", message);
            log.info("Broadcasted INCIDENT_CREATED: {}", incident.getId());
        } catch (Exception e) {
            log.error("Error broadcasting incident created", e);
        }
    }

    /**
     * Broadcast incident confirmed event
     */
    public void broadcastIncidentConfirmed(IncidentResponse incident) {
        try {
            WebSocketMessage message = WebSocketMessage.builder()
                    .type("INCIDENT_CONFIRMED")
                    .data(incident)
                    .timestamp(System.currentTimeMillis())
                    .build();
            messagingTemplate.convertAndSend("/topic/incidents", message);
            log.info("Broadcasted INCIDENT_CONFIRMED: {}", incident.getId());
        } catch (Exception e) {
            log.error("Error broadcasting incident confirmed", e);
        }
    }

    /**
     * Broadcast incident resolved event
     */
    public void broadcastIncidentResolved(IncidentResponse incident) {
        try {
            WebSocketMessage message = WebSocketMessage.builder()
                    .type("INCIDENT_RESOLVED")
                    .data(incident)
                    .timestamp(System.currentTimeMillis())
                    .build();
            messagingTemplate.convertAndSend("/topic/incidents", message);
            log.info("Broadcasted INCIDENT_RESOLVED: {}", incident.getId());
        } catch (Exception e) {
            log.error("Error broadcasting incident resolved", e);
        }
    }

    /**
     * Broadcast image added to incident
     */
    public void broadcastImageAdded(Long incidentId, IncidentResponse incident) {
        try {
            WebSocketMessage message = WebSocketMessage.builder()
                    .type("IMAGE_ADDED")
                    .data(incident)
                    .timestamp(System.currentTimeMillis())
                    .build();
            messagingTemplate.convertAndSend("/topic/incidents", message);
            log.info("Broadcasted IMAGE_ADDED to incident: {}", incidentId);
        } catch (Exception e) {
            log.error("Error broadcasting image added", e);
        }
    }
}
