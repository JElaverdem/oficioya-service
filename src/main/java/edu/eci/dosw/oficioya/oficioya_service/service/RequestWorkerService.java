package edu.eci.dosw.oficioya.oficioya_service.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import edu.eci.dosw.oficioya.oficioya_service.entity.Request;
import edu.eci.dosw.oficioya.oficioya_service.model.RequestState;
import edu.eci.dosw.oficioya.oficioya_service.repository.RequestRepository;

@Service
public class RequestWorkerService {

    private final RequestRepository requestRepository;

    public RequestWorkerService(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    public void acceptRequest(UUID requestId, UUID workerId) {
        Request request = requestRepository.findById(requestId).orElseThrow();
        if (!request.getWorkerId().equals(workerId)) {
            throw new RuntimeException("Solo el trabajador asignado puede aceptar esta solicitud");
        }
        if (request.getState() != RequestState.PENDING) {
            throw new RuntimeException("Solo se pueden aceptar solicitudes en estado PENDING");
        }
        if (request.getDateCreated() != null && request.getDateCreated().plusMinutes(30).isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Tiempo de respuesta excedido (30 minutos)");
        }
        request.setState(RequestState.ACCEPTED);
        requestRepository.save(request);
    }

    public void rejectRequest(UUID requestId, UUID workerId) {
        Request request = requestRepository.findById(requestId).orElseThrow();
        if (!request.getWorkerId().equals(workerId)) {
            throw new RuntimeException("Solo el trabajador asignado puede rechazar esta solicitud");
        }
        if (request.getState() != RequestState.PENDING) {
            throw new RuntimeException("Solo se pueden rechazar solicitudes en estado PENDING");
        }
        if (request.getDateCreated() != null && request.getDateCreated().plusMinutes(30).isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Tiempo de respuesta excedido (30 minutos)");
        }
        request.setState(RequestState.REJECTED);
        requestRepository.save(request);
    }
}
