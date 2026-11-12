package edu.eci.dosw.oficioya.oficioya_service.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import edu.eci.dosw.oficioya.oficioya_service.entity.Request;
import edu.eci.dosw.oficioya.oficioya_service.mapper.RequestMapper;
import edu.eci.dosw.oficioya.oficioya_service.model.RequestModel;
import edu.eci.dosw.oficioya.oficioya_service.model.RequestState;
import edu.eci.dosw.oficioya.oficioya_service.repository.RequestRepository;

@Service
public class RequestCompletionService {

    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;
    private final NotificationService notificationService;

    public RequestCompletionService(RequestRepository requestRepository, RequestMapper requestMapper, NotificationService notificationService) {
        this.requestRepository = requestRepository;
        this.requestMapper = requestMapper;
        this.notificationService = notificationService;
    }

    public RequestModel completeRequest(UUID requestId, UUID workerId) {
        Request request = requestRepository.findById(requestId).orElseThrow();
        if (!request.getWorkerId().equals(workerId)) {
            throw new RuntimeException("Solo el trabajador asignado puede completar esta solicitud");
        }
        if (request.getState() != RequestState.ACCEPTED) {
            throw new RuntimeException("Solo se pueden completar solicitudes en estado ACCEPTED");
        }
        request.setState(RequestState.COMPLETED);
        // Notificar al contratante que el trabajo fue completado (microservicio de notificaciones)
        notificationService.notifyCompleted();

        return requestMapper.toModel(requestRepository.save(request));
    }
}
