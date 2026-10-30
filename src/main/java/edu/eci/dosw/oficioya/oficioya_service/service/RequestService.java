package edu.eci.dosw.oficioya.oficioya_service.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.eci.dosw.oficioya.oficioya_service.entity.Request;
import edu.eci.dosw.oficioya.oficioya_service.model.CancellationState;
import edu.eci.dosw.oficioya.oficioya_service.model.RequestState;
import edu.eci.dosw.oficioya.oficioya_service.repository.RequestRepository;

@Service
public class RequestService {

    private final RequestRepository requestRepository;

    public RequestService(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    @Transactional
    public List<Request> createRequest(UUID contractorId,
                                       List<UUID> workerIds,
                                       String description,
                                       String neighborhood,
                                       String exactAddress,
                                       LocalDateTime scheduledDateTime) {

        UUID rootId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        List<Request> createdRequests = new ArrayList<>();

        for (UUID workerId : workerIds) {
            Optional<Request> blockedRequest = requestRepository.findContractorHasCancelledWorker(contractorId, workerId);

            if (blockedRequest.isPresent()) {
                Request blocked = blockedRequest.get();

                if (blocked.getCancellationState() == CancellationState.IN_PROGRESS && blocked.getCancellationDateTime() != null) {
                    LocalDateTime blockExpiresAt = blocked.getCancellationDateTime().plusDays(7);

                    if (now.isBefore(blockExpiresAt)) {
                        throw new RuntimeException("No puedes enviar solicitud a este trabajador por bloqueo de 1 semana.");
                    }

                    if (now.isAfter(blockExpiresAt)) {
                        blocked.setState(RequestState.CANCELED);
                        requestRepository.save(blocked);
                    }
                }
            }

            Request request = new Request();
            request.setId(UUID.randomUUID());
            request.setRootId(rootId);
            request.setContractorId(contractorId);
            request.setWorkerId(workerId);
            request.setDescription(description);
            request.setNeighborhood(neighborhood);
            request.setExactAddress(exactAddress);
            request.setScheduledDateTime(scheduledDateTime);
            request.setState(RequestState.PENDING);
            request.setDateCreated(now);
            request.setCancellationState(null);
            request.setCancellationDateTime(null);

            createdRequests.add(requestRepository.save(request));
        }

        return createdRequests;
    }
}
