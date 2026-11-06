package edu.eci.dosw.oficioya.oficioya_service.service;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class LoggingNotificationServiceTest {

    @Test
    void callsDoNotThrow() {
        LoggingNotificationService service = new LoggingNotificationService();
        UUID id = UUID.randomUUID();
        service.notifyNewRequest(id, id);
        service.notifyAccepted(id, id);
        service.notifyCancelled(id, id);
        service.notifyCompleted(id, id);
    }
}