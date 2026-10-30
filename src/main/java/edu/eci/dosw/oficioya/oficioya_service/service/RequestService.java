package edu.eci.dosw.oficioya.oficioya_service.service;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.eci.dosw.oficioya.oficioya_service.entity.Request;
import edu.eci.dosw.oficioya.oficioya_service.exception.RecursoNoEncontradoException;
import edu.eci.dosw.oficioya.oficioya_service.exception.TransicionInvalidaException;
import edu.eci.dosw.oficioya.oficioya_service.mapper.RequestMapper;
import edu.eci.dosw.oficioya.oficioya_service.model.CancellationState;
import edu.eci.dosw.oficioya.oficioya_service.model.RequestModel;
import edu.eci.dosw.oficioya.oficioya_service.model.RequestState;
import edu.eci.dosw.oficioya.oficioya_service.repository.RequestRepository;

@Service
public class RequestService {

    private static final long MINUTOS_PARA_RESPONDER = 30;
    private static final String SOLICITUD_NO_ENCONTRADA = "Solicitud no encontrada";
    private static final Set<RequestState> CANCELABLES_EN_CONFLICTO =
            EnumSet.of(RequestState.PENDING, RequestState.ACCEPTED, RequestState.CONFLICT);

    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;

    public RequestService(RequestRepository requestRepository, RequestMapper requestMapper) {
        this.requestRepository = requestRepository;
        this.requestMapper = requestMapper;
    }

    /** Todas las solicitudes (no eliminadas) donde el usuario es contratante o trabajador. */
    @Transactional(readOnly = true)
    public List<RequestModel> listRequestsForUser(UUID userId) {
        return requestRepository.findAllRequestsForUser(userId).stream()
                .map(requestMapper::toModel)
                .toList();
    }

    /**
     * El trabajador asignado acepta una solicitud PENDING dentro de los 30 minutos.
     * Si otra solicitud del mismo rootId ya estaba aceptada (o en conflicto), la solicitud
     * y esas otras pasan a CONFLICT para que el contratante elija una (acceptConflict).
     */
    @Transactional
    public RequestModel acceptRequest(UUID requestId, UUID workerId) {
        Request request = findAssignedToWorker(requestId, workerId);
        requireAnswerable(request);

        List<Request> accepted = requestRepository.findByRootId(request.getRootId()).stream()
                .filter(r -> !r.getId().equals(request.getId()))
                .filter(r -> r.getState() == RequestState.ACCEPTED || r.getState() == RequestState.CONFLICT)
                .toList();

        if (accepted.isEmpty()) {
            request.setState(RequestState.ACCEPTED);
        } else {
            request.setState(RequestState.CONFLICT);
            accepted.forEach(r -> r.setState(RequestState.CONFLICT));
            requestRepository.saveAll(accepted);
        }
        return requestMapper.toModel(requestRepository.save(request));
    }

    /** El trabajador asignado rechaza una solicitud PENDING dentro de los 30 minutos. */
    @Transactional
    public RequestModel rejectRequest(UUID requestId, UUID workerId) {
        Request request = findAssignedToWorker(requestId, workerId);
        requireAnswerable(request);
        request.setState(RequestState.REJECTED);
        return requestMapper.toModel(requestRepository.save(request));
    }

    /**
     * El contratante resuelve un conflicto eligiendo una solicitud (en estado CONFLICT):
     * esa pasa a ACCEPTED y las demás del mismo rootId se cancelan sin penalización.
     */
    @Transactional
    public RequestModel acceptConflict(UUID requestId, UUID contractorId) {
        Request chosen = requestRepository.findById(requestId)
                .filter(r -> r.getContractorId().equals(contractorId))
                .orElseThrow(() -> new RecursoNoEncontradoException(SOLICITUD_NO_ENCONTRADA));
        if (chosen.getState() != RequestState.CONFLICT) {
            throw new TransicionInvalidaException("Solo se puede resolver un conflicto sobre una solicitud en estado CONFLICT");
        }

        LocalDateTime now = LocalDateTime.now();
        List<Request> others = requestRepository.findByRootId(chosen.getRootId()).stream()
                .filter(r -> !r.getId().equals(chosen.getId()))
                .filter(r -> CANCELABLES_EN_CONFLICTO.contains(r.getState()))
                .toList();
        for (Request other : others) {
            other.setState(RequestState.CANCELED);
            other.setCancellationState(CancellationState.COMPLETED);
            other.setCancellationDateTime(now);
        }
        requestRepository.saveAll(others);

        chosen.setState(RequestState.ACCEPTED);
        return requestMapper.toModel(requestRepository.save(chosen));
    }

    // Una solicitud ajena responde igual que una inexistente (404) para no revelar que existe.
    private Request findAssignedToWorker(UUID requestId, UUID workerId) {
        return requestRepository.findById(requestId)
                .filter(r -> r.getWorkerId().equals(workerId))
                .orElseThrow(() -> new RecursoNoEncontradoException(SOLICITUD_NO_ENCONTRADA));
    }

    private void requireAnswerable(Request request) {
        if (request.getState() != RequestState.PENDING) {
            throw new TransicionInvalidaException("Solo se pueden responder solicitudes en estado PENDING");
        }
        if (request.getDateCreated() != null
                && request.getDateCreated().plusMinutes(MINUTOS_PARA_RESPONDER).isBefore(LocalDateTime.now())) {
            throw new TransicionInvalidaException("Tiempo de respuesta excedido (30 minutos)");
        }
    }
}
