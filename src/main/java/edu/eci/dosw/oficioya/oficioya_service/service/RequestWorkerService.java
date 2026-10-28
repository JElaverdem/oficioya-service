package edu.eci.dosw.oficioya.oficioya_service.service;

import java.util.UUID;

public interface RequestWorkerService {

    void acceptRequest(UUID requestId, UUID workerId);

    void rejectRequest(UUID requestId, UUID workerId);
}
