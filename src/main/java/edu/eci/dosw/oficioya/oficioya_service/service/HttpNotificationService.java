package edu.eci.dosw.oficioya.oficioya_service.service;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class HttpNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(HttpNotificationService.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public HttpNotificationService(@Value("${notification.service.url:http://localhost:8082}") String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    @Override
    public void notifyNewRequest(UUID workerId, UUID requestId) {
        send(workerId, "INCOMING_REQUEST", requestId, "Nueva solicitud de servicio cerca de ti");
    }

    @Override
    public void notifyAccepted(UUID contractorId, UUID requestId) {
        send(contractorId, "ACCEPTED_REQUEST", requestId, "Tu solicitud fue aceptada");
    }

    @Override
    public void notifyCancelled(UUID receiverId, UUID requestId) {
        send(receiverId, "CANCELLED_BY_CONTRACTOR", requestId, "Una solicitud fue cancelada");
    }

    @Override
    public void notifyCompleted(UUID contractorId, UUID requestId) {
        send(contractorId, "COMPLETED_WORK", requestId, "El trabajo fue reportado como terminado");
    }

    private void send(UUID receiverId, String type, UUID requestId, String message) {
        try {
            Map<String, Object> payload = Map.of(
                    "receiverId", receiverId,
                    "type", type,
                    "requestId", requestId,
                    "message", message
            );
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(baseUrl + "/api/v1/notification", entity, String.class);
        } catch (Exception e) {
            log.warn("No se pudo enviar notificación a {} ({}): {}", baseUrl, type, e.getMessage());
        }
    }
}
