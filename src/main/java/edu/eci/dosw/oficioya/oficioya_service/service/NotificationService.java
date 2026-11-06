package edu.eci.dosw.oficioya.oficioya_service.service;

import java.util.UUID;

public interface NotificationService {
    void notifyNewRequest(UUID workerId, UUID requestId);
    void notifyAccepted(UUID contractorId, UUID requestId);
    void notifyCancelled(UUID receiverId, UUID requestId);
    void notifyCompleted(UUID contractorId, UUID requestId);
}