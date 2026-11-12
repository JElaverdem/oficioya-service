package edu.eci.dosw.oficioya.oficioya_service.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

// Reemplazado por HttpNotificationService para la integración real con oficioya-notification.
// Se conserva la clase para compatibilidad con tests existentes.
public class LoggingNotificationService implements NotificationService {

    @Override
    public void notifyNewRequest(UUID workerId, UUID requestId) {
        System.out.println("Notificacion: nueva solicitud " + requestId + " para trabajador " + workerId);
    }

    @Override
    public void notifyAccepted(UUID contractorId, UUID requestId) {
        System.out.println("Notificacion: solicitud aceptada " + requestId + " para contratante " + contractorId);
    }

    @Override
    public void notifyCancelled(UUID receiverId, UUID requestId) {
        System.out.println("Notificacion: solicitud cancelada " + requestId + " para " + receiverId);
    }

    @Override
    public void notifyCompleted(UUID contractorId, UUID requestId) {
        System.out.println("Notificacion: trabajo completado " + requestId + " para contratante " + contractorId);
    }
}