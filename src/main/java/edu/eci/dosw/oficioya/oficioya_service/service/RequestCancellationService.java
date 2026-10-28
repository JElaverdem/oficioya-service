package edu.eci.dosw.oficioya.oficioya_service.service;

import edu.eci.dosw.oficioya.oficioya_service.dto.request.CancelRequestDTO;

import java.util.UUID;

public interface RequestCancellationService {

    void cancelByClient(UUID requestId, UUID contractorId, CancelRequestDTO dto);

    void cancelByWorker(UUID requestId, UUID workerId, CancelRequestDTO dto);
}
