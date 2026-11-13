package edu.eci.dosw.oficioya.oficioya_service.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.eci.dosw.oficioya.oficioya_service.dto.RequestRequest;
import edu.eci.dosw.oficioya.oficioya_service.dto.RequestResponse;
import edu.eci.dosw.oficioya.oficioya_service.dto.ServiceReportResponse;
import edu.eci.dosw.oficioya.oficioya_service.dto.UserResponse;
import edu.eci.dosw.oficioya.oficioya_service.dto.WorkerStatsResponse;
import edu.eci.dosw.oficioya.oficioya_service.entity.Request;
import edu.eci.dosw.oficioya.oficioya_service.exception.RecursoNoEncontradoException;
import edu.eci.dosw.oficioya.oficioya_service.exception.RequestSinWorkersException;
import edu.eci.dosw.oficioya.oficioya_service.exception.TransicionInvalidaException;
import edu.eci.dosw.oficioya.oficioya_service.mapper.RequestMapper;
import edu.eci.dosw.oficioya.oficioya_service.mapper.ResponseMapper;
import edu.eci.dosw.oficioya.oficioya_service.model.CancellationState;
import edu.eci.dosw.oficioya.oficioya_service.model.RequestModel;
import edu.eci.dosw.oficioya.oficioya_service.model.RequestState;
import edu.eci.dosw.oficioya.oficioya_service.repository.RequestRepository;

@Service
public class RequestService {

    private static final long MINUTOS_PARA_RESPONDER = 30;
    private static final Set<RequestState> CANCELABLES_EN_CONFLICTO =
            EnumSet.of(RequestState.PENDING, RequestState.ACCEPTED, RequestState.CONFLICT);

    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;
    private final ResponseMapper responseMapper;
    private final NotificationService notificationService;
    private final UserClient userClient;

    public RequestService(RequestRepository requestRepository, RequestMapper requestMapper, ResponseMapper responseMapper, NotificationService notificationService, UserClient userClient) {
        this.requestRepository = requestRepository;
        this.requestMapper = requestMapper;
        this.responseMapper = responseMapper;
        this.notificationService = notificationService;
        this.userClient = userClient;
    }

    public RequestResponse create(RequestRequest req, UUID contractorId){
        List<UUID> workers = req.workersId();

        if(workers.size() <= 0){
            throw new RequestSinWorkersException();
        }

        if (userClient != null) {
            UserResponse contractor = userClient.getUser(contractorId);
            if (contractor == null || contractor.roles() == null || !contractor.roles().contains("CONTRATANTE")) {
                throw new RecursoNoEncontradoException("Contratante no válido o inactivo");
            }
        }

        List<UUID> verifiedWorkers = new ArrayList<>();
        UserResponse worker = null;

        for (UUID id : workers){
            worker = userClient.getUser(id);

            if (worker == null || worker.roles() == null || !worker.roles().contains("TRABAJADOR")) {
                continue;
            }

            Optional<Request> contractorCancelledWorker = requestRepository.findContractorHasCancelledWorker(contractorId, id);

            if(!contractorCancelledWorker.isEmpty()){
                LocalDateTime cancellationDate = contractorCancelledWorker.orElse(null).getCancellationDateTime();
                if(LocalDateTime.now().isAfter(cancellationDate.plusWeeks(1))){
                    contractorCancelledWorker.orElse(null).setCancellationState(CancellationState.COMPLETED);
                }
                else{
                    continue;
                }
            }

            //TODO: AGREGAR FRANJA HORARIA DEL WORKER
            //Si está libre se agrega
            if(true){
                verifiedWorkers.add(id);
            }
        }

        if (verifiedWorkers.size() <= 0){
            throw new RecursoNoEncontradoException(RecursoNoEncontradoException.TRABAJADOR_NO_ENCONTRADO);
        }
        
        UUID newRootId = UUID.randomUUID();

        while (requestRepository.findByRootId(newRootId).size() >= 1){
            newRootId = UUID.randomUUID();
        }

        List<Request> createdRequests = responseMapper.toEntities(contractorId, verifiedWorkers, req, newRootId);

        createdRequests.forEach(w -> notificationService.notifyNewRequest(w.getWorkerId(), w.getId()));

        return responseMapper.toRespone(requestRepository.saveAll(createdRequests));

    }


    /** Todas las solicitudes (no eliminadas) donde el usuario es contratante o trabajador. */
    @Transactional(readOnly = true)
    public List<RequestModel> listRequestsForUser(UUID userId) {
        return requestRepository.findAllRequestsForUser(userId).stream()
                .map(requestMapper::toModel)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RequestModel> listRequestsForWorker(UUID workerId) {
        return requestRepository.findAllRequestsForWorker(workerId).stream()
                .map(requestMapper::toModel)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RequestModel> listRequestsForContractor(UUID contractorId) {
        return requestRepository.findAllRequestsForContractor(contractorId).stream()
                .map(requestMapper::toModel)
                .toList();
    }

    /** Estadísticas del trabajador para otros dominios (completedJobs, monthlyEarnings, topServices). */
    @Transactional(readOnly = true)
    public WorkerStatsResponse getWorkerStats(UUID workerId) {
        long completed = requestRepository.findAllRequestsForWorker(workerId).stream()
                .filter(r -> r.getState() == RequestState.COMPLETED)
                .count();
        return new WorkerStatsResponse(workerId, completed, 0.0, List.of());
    }

    /** Reporte de solicitudes por período: por estado, franja horaria y barrio. */
    @Transactional(readOnly = true)
    public ServiceReportResponse getReport(LocalDateTime from, LocalDateTime to) {
        List<Request> requests = requestRepository.findAll().stream()
                .filter(r -> r.getScheduledDateTime() != null
                        && !r.getScheduledDateTime().isBefore(from)
                        && !r.getScheduledDateTime().isAfter(to))
                .toList();

        Map<String, Long> byStatus = requests.stream()
                .collect(Collectors.groupingBy(r -> r.getState().name(), Collectors.counting()));

        Map<String, Long> byNeighborhood = requests.stream()
                .collect(Collectors.groupingBy(Request::getNeighborhood, Collectors.counting()));

        Map<Integer, Long> byHour = requests.stream()
                .collect(Collectors.groupingBy(r -> r.getScheduledDateTime().getHour(), Collectors.counting()));

        return new ServiceReportResponse(requests.size(), byStatus, byNeighborhood, byHour);
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
            notificationService.notifyAccepted(request.getContractorId(),requestId);
        } else {
            request.setState(RequestState.CONFLICT);
            accepted.forEach(r -> r.setState(RequestState.CONFLICT));
            requestRepository.saveAll(accepted);
            //notificationService;???
        }
        return requestMapper.toModel(requestRepository.save(request));
    }

    /** El trabajador asignado rechaza una solicitud PENDING dentro de los 30 minutos. */
    @Transactional
    public RequestModel rejectRequest(UUID requestId, UUID workerId) {
        Request request = findAssignedToWorker(requestId, workerId);
        requireAnswerable(request);
        request.setState(RequestState.REJECTED);
        notificationService.notifyRejected(request.getContractorId(), requestId);
        //TODO: ELIMINAR FRANJA HORARIA DEL WORKER
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
                .orElseThrow(() -> new RecursoNoEncontradoException(RecursoNoEncontradoException.SOLICITUD_NO_ENCONTRADA));
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
        
        others.forEach(r -> notificationService.notifyCancelled(r.getContractorId(), requestId));

        //TODO: ELIMINAR FRANJA HORARIA DEL WORKER
        return requestMapper.toModel(requestRepository.save(chosen));
    }

    public boolean deleteRequest(UUID requestId){
        Optional<Request> found = requestRepository.findById(requestId);

        if(found.isEmpty()){
            throw new RecursoNoEncontradoException(RecursoNoEncontradoException.SOLICITUD_NO_ENCONTRADA);
        }

        int changed = requestRepository.specialDeleteById(requestId);

        if(changed == 1){
            return true;
        }
        else{
            return false;
        }
    }

    public boolean existsById(UUID requestId){
        Optional<Request> found = requestRepository.findById(requestId);
        if(!found.isEmpty() && found.orElse(null).getState() != RequestState.DELETED){
            return true;
        }
        else{
            return false;
        }
    }

    public Optional<Request> getRequest(UUID requestId){
        return requestRepository.findById(requestId);
    }

    public Optional<UUID> getRootId(UUID requestId){
        Optional<Request> found = requestRepository.findById(requestId);
        if(!found.isEmpty()){
            return Optional.of(found.orElse(null).getRootId());
        }
        else{
            return Optional.empty();
        }
    }

    // Una solicitud ajena responde igual que una inexistente (404) para no revelar que existe.
    private Request findAssignedToWorker(UUID requestId, UUID workerId) {
        return requestRepository.findById(requestId)
                .filter(r -> r.getWorkerId().equals(workerId))
                .orElseThrow(() -> new RecursoNoEncontradoException(RecursoNoEncontradoException.SOLICITUD_NO_ENCONTRADA));
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
