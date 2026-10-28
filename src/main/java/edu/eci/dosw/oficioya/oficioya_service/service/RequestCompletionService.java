package edu.eci.dosw.oficioya.oficioya_service.service;

import java.util.UUID;

public interface RequestCompletionService {

    void completeRequest(UUID requestId, UUID workerId);
}
