package edu.eci.dosw.oficioya.oficioya_service.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import edu.eci.dosw.oficioya.oficioya_service.dto.UserResponse;
import edu.eci.dosw.oficioya.oficioya_service.entity.Request;
import edu.eci.dosw.oficioya.oficioya_service.exception.CredencialesInvalidasException;
import edu.eci.dosw.oficioya.oficioya_service.exception.RecursoNoEncontradoException;
import edu.eci.dosw.oficioya.oficioya_service.exception.RecursosAjenosException;
import edu.eci.dosw.oficioya.oficioya_service.mapper.RequestMapper;
import edu.eci.dosw.oficioya.oficioya_service.model.CancellationState;
import edu.eci.dosw.oficioya.oficioya_service.model.RequestModel;
import edu.eci.dosw.oficioya.oficioya_service.model.RequestState;
import edu.eci.dosw.oficioya.oficioya_service.repository.RequestRepository;

@Service
public class RequestCancellationService {

    private final RequestRepository requestRepository;

    private final RequestMapper requestMapper;

    private final NotificationService notificationService;

    private final UserClient userClient;

    public RequestCancellationService(RequestRepository requestRepository, RequestMapper requestMapper, NotificationService notificationService, UserClient userClient) {
        this.requestRepository = requestRepository;
        this.requestMapper = requestMapper;
        this.notificationService = notificationService;
        this.userClient = userClient;
    }

    public RequestModel cancelRequest(UUID requestId, UUID userId){
        if(requestRepository.findById(requestId).isEmpty()){
            throw new RecursoNoEncontradoException("NO_ENCONTRADO");
        }

        if (userClient != null) {
            UserResponse user = userClient.getUser(userId);
            if (user == null || user.roles() == null) {
                throw new RecursoNoEncontradoException("Usuario no válido o inactivo");
            }
        }

        Request found = requestRepository.findById(requestId).orElse(null);

        if(userId != found.getContractorId() && userId != found.getWorkerId()){
            throw new RecursosAjenosException();
        }

        if(userId == found.getContractorId()){
            cancelByClient(requestId, userId);
        }
        else if(userId == found.getWorkerId()){
            cancelByWorker(requestId, userId);
        }

        return requestMapper.toModel(requestRepository.findById(requestId).orElse(null));
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
                request.setCancellationState(CancellationState.IN_PROGRESS);
            }
            else{
                request.setCancellationState(CancellationState.COMPLETED);
            }
        }

        request.setState(RequestState.CANCELED);
        if (request.getCancellationState() == null) {
            request.setCancellationState(CancellationState.COMPLETED);
        }
        request.setCancellationDateTime(LocalDateTime.now());
        requestRepository.save(request);
        // Notificar al trabajador que la solicitud fue cancelada (microservicio de notificaciones)
        notificationService.notifyCancelled(request.getWorkerId(), requestId);

        //TODO: ELIMINAR FRANJA HORARIA DEL WORKER

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
                // Penalizacion de reputacion: -0.5 puntos al trabajador (se delega al microservicio de reputacion)
                penalizarReputacion(workerId);
            }
        }
        request.setState(RequestState.CANCELED);
        request.setCancellationState(CancellationState.COMPLETED);
        request.setCancellationDateTime(LocalDateTime.now());
        requestRepository.save(request);
        // Notificar al contratante que la solicitud fue cancelada (microservicio de notificaciones)
        notificationService.notifyCancelled(request.getContractorId(), requestId);

        //TODO: ELIMINAR FRANJA HORARIA DEL WORKER
    }
    private void penalizarReputacion(UUID workerId) {
        // TODO: invocar microservicio de reputacion para restar 0.5 puntos
        System.out.println("Penalizacion de reputacion -0.5 para trabajador: " + workerId);
    }

}