package edu.eci.dosw.oficioya.oficioya_service.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import edu.eci.dosw.oficioya.oficioya_service.entity.Request;
import edu.eci.dosw.oficioya.oficioya_service.model.CancellationState;
import edu.eci.dosw.oficioya.oficioya_service.model.RequestState;
import edu.eci.dosw.oficioya.oficioya_service.repository.RequestRepository;

@Service
public class RequestCancellationService {

    private final RequestRepository requestRepository;

    public RequestCancellationService(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    public void cancelByClient(UUID requestId, UUID contractorId) {
        Request request = requestRepository.findById(requestId).orElseThrow();
        if (!request.getContractorId().equals(contractorId)) {
            throw new RuntimeException("Solo el contratante puede cancelar esta solicitud");
        }
        if (request.getState() == RequestState.COMPLETED) {
            throw new RuntimeException("No se puede cancelar a solicitud ya completada");
        }
        if (request.getState() == RequestState.CANCELED || request.getState() == RequestState.DELETED) {
            throw new RuntimeException("Esta solicitud ya se encuentra cancelada");
        }
        if (request.getState() == RequestState.ACCEPTED && request.getScheduledDateTime() != null) {
            LocalDateTime limit = request.getScheduledDateTime().minusHours(24);
            if (LocalDateTime.now().isAfter(limit)) {
                throw new RuntimeException("Debe cancelarse al menos 24 horas antes del servicio");
            }
        }
        request.setState(RequestState.CANCELED);
        request.setCancellationState(CancellationState.COMPLETED);
        request.setCancellationDateTime(LocalDateTime.now());
        requestRepository.save(request);
    }

    public void cancelByWorker(UUID requestId, UUID workerId) {
        Request request = requestRepository.findById(requestId).orElseThrow();
        if (!request.getWorkerId().equals(workerId)) {
            throw new RuntimeException("Solo el trabajador asignado puede cancelar esta solicitud");
        }
        if (request.getState() == RequestState.COMPLETED) {
            throw new RuntimeException("No se puede cancelar a solicitud ya completada");
        }
        if (request.getState() == RequestState.CANCELED || request.getState() == RequestState.DELETED) {
            throw new RuntimeException("Esta solicitud ya se encuentra cancelada");
        }
        if (request.getState() == RequestState.ACCEPTED && request.getScheduledDateTime() != null) {
            LocalDateTime limit = request.getScheduledDateTime().minusHours(24);
            if (LocalDateTime.now().isAfter(limit)) {
                throw new RuntimeException("Debe cancelarse al menos 24 horas antes del servicio");
            }
        }
        request.setState(RequestState.CANCELED);
        request.setCancellationState(CancellationState.COMPLETED);
        request.setCancellationDateTime(LocalDateTime.now());
        requestRepository.save(request);
    }
}
