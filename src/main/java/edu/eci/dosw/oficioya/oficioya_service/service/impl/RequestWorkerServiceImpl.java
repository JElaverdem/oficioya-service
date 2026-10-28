package edu.eci.dosw.oficioya.oficioya_service.service.impl;

import edu.eci.dosw.oficioya.oficioya_service.entity.Request;
import edu.eci.dosw.oficioya.oficioya_service.model.RequestState;
import edu.eci.dosw.oficioya.oficioya_service.repository.RequestRepository;
import edu.eci.dosw.oficioya.oficioya_service.service.RequestWorkerService;
import edu.eci.dosw.oficioya.oficioya_service.shared.exception.InvalidStateTransitionException;
import edu.eci.dosw.oficioya.oficioya_service.shared.exception.NotAllowedException;
import edu.eci.dosw.oficioya.oficioya_service.shared.exception.RequestNotFoundException;
import edu.eci.dosw.oficioya.oficioya_service.shared.exception.RequestExpiredException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class RequestWorkerServiceImpl implements RequestWorkerService {

    private final RequestRepository requestRepository;

    public RequestWorkerServiceImpl(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    @Override
    public void acceptRequest(UUID requestId, UUID workerId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException("Solicitud no encontrada con id: " + requestId));

        validateWorker(request, workerId);
        validatePending(request);
        validateTimeLimit(request);

        request.setState(RequestState.ACCEPTED);
        requestRepository.save(request);
    }

    @Override
    public void rejectRequest(UUID requestId, UUID workerId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException("Solicitud no encontrada con id: " + requestId));

        validateWorker(request, workerId);
        validatePending(request);
        validateTimeLimit(request);

        request.setState(RequestState.REJECTED);
        requestRepository.save(request);
    }

    private void validateWorker(Request request, UUID workerId) {
        if (!request.getWorkerId().equals(workerId)) {
            throw new NotAllowedException("Solo el trabajador asignado puede responder esta solicitud");
        }
    }

    private void validatePending(Request request) {
        if (request.getState() != RequestState.PENDING) {
            throw new InvalidStateTransitionException("Solo se puede aceptar o rechazar solicitudes en estado PENDING");
        }
    }

    private void validateTimeLimit(Request request) {
        LocalDateTime limit = request.getDateCreated().plusMinutes(30);
        if (LocalDateTime.now().isAfter(limit) {
            throw new RequestExpiredException("El tiempo para responder esta solicitud ha expirado (30 minutos)");
        }
    }
}
