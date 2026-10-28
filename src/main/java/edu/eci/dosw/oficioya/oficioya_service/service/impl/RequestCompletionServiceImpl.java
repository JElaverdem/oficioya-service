package edu.eci.dosw.oficioya.oficioya_service.service.impl;

import edu.eci.dosw.oficioya.oficioya_service.entity.Request;
import edu.eci.dosw.oficioya.oficioya_service.model.RequestState;
import edu.eci.dosw.oficioya.oficioya_service.repository.RequestRepository;
import edu.eci.dosw.oficioya.oficioya_service.service.RequestCompletionService;
import edu.eci.dosw.oficioya.oficioya_service.shared.exception.InvalidStateTransitionException;
import edu.eci.dosw.oficioya.oficioya_service.shared.exception.NotAllowedException;
import edu.eci.dosw.oficioya.oficioya_service.shared.exception.RequestNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class RequestCompletionServiceImpl implements RequestCompletionService {

    private final RequestRepository requestRepository;

    public RequestCompletionServiceImpl(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    @Override
    public void completeRequest(UUID requestId, UUID workerId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException("Solicitud no encontrada con id: " + requestId));

        if (!request.getWorkerId().equals(workerId)) {
            throw new NotAllowedException("Solo el trabajador asignado puede marcar esta solicitud como completada");
        }

        if (request.getState() != RequestState.ACCEPTED) {
            throw new InvalidStateTransitionException("Solo se pueden completar solicitudes en estado ACCEPTED");
        }

        request.setState(RequestState.COMPLETED);
        requestRepository.save(request);
    }
}
