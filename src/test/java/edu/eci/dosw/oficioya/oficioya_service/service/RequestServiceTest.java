package edu.eci.dosw.oficioya.oficioya_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import edu.eci.dosw.oficioya.oficioya_service.entity.Request;
import edu.eci.dosw.oficioya.oficioya_service.exception.RecursoNoEncontradoException;
import edu.eci.dosw.oficioya.oficioya_service.exception.TransicionInvalidaException;
import edu.eci.dosw.oficioya.oficioya_service.mapper.RequestMapper;
import edu.eci.dosw.oficioya.oficioya_service.model.CancellationState;
import edu.eci.dosw.oficioya.oficioya_service.model.RequestModel;
import edu.eci.dosw.oficioya.oficioya_service.model.RequestState;
import edu.eci.dosw.oficioya.oficioya_service.repository.RequestRepository;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private RequestMapper requestMapper;

    @InjectMocks
    private RequestService requestService;

    @Test
    @DisplayName("listRequestsForUser mapea todas las solicitudes del usuario")
    void listarMapeaTodasLasSolicitudes() {
        UUID userId = UUID.randomUUID();
        Request first = request(RequestState.PENDING, UUID.randomUUID(), LocalDateTime.now());
        Request second = request(RequestState.ACCEPTED, UUID.randomUUID(), LocalDateTime.now());
        RequestModel firstModel = new RequestModel();
        RequestModel secondModel = new RequestModel();
        when(requestRepository.findAllRequestsForUser(userId)).thenReturn(List.of(first, second));
        when(requestMapper.toModel(first)).thenReturn(firstModel);
        when(requestMapper.toModel(second)).thenReturn(secondModel);

        List<RequestModel> result = requestService.listRequestsForUser(userId);

        assertEquals(2, result.size());
        assertSame(firstModel, result.get(0));
        assertSame(secondModel, result.get(1));
    }

    @Test
    @DisplayName("listRequestsForUser devuelve lista vacia cuando no hay solicitudes")
    void listarVacio() {
        UUID userId = UUID.randomUUID();
        when(requestRepository.findAllRequestsForUser(userId)).thenReturn(List.of());

        assertTrue(requestService.listRequestsForUser(userId).isEmpty());
    }

    @Test
    @DisplayName("acceptRequest pasa de PENDING a ACCEPTED cuando no hay conflicto")
    void aceptarSinConflicto() {
        UUID workerId = UUID.randomUUID();
        Request pending = request(RequestState.PENDING, workerId, LocalDateTime.now().minusMinutes(5));
        RequestModel model = new RequestModel();
        when(requestRepository.findById(pending.getId())).thenReturn(Optional.of(pending));
        when(requestRepository.findByRootId(pending.getRootId())).thenReturn(List.of(pending));
        when(requestRepository.save(pending)).thenReturn(pending);
        when(requestMapper.toModel(pending)).thenReturn(model);

        RequestModel result = requestService.acceptRequest(pending.getId(), workerId);

        assertSame(model, result);
        assertEquals(RequestState.ACCEPTED, pending.getState());
        verify(requestRepository).save(pending);
        verify(requestRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("acceptRequest acepta una solicitud creada hace menos de 30 minutos")
    void aceptarDentroDelTiempoLimite() {
        UUID workerId = UUID.randomUUID();
        Request pending = request(RequestState.PENDING, workerId, LocalDateTime.now().minusMinutes(29));
        when(requestRepository.findById(pending.getId())).thenReturn(Optional.of(pending));
        when(requestRepository.findByRootId(pending.getRootId())).thenReturn(List.of(pending));
        when(requestRepository.save(pending)).thenReturn(pending);
        when(requestMapper.toModel(pending)).thenReturn(new RequestModel());

        requestService.acceptRequest(pending.getId(), workerId);

        assertEquals(RequestState.ACCEPTED, pending.getState());
    }

    @Test
    @DisplayName("acceptRequest sin fecha de creacion no aplica el limite de tiempo")
    void aceptarSinFechaDeCreacion() {
        UUID workerId = UUID.randomUUID();
        Request pending = request(RequestState.PENDING, workerId, null);
        when(requestRepository.findById(pending.getId())).thenReturn(Optional.of(pending));
        when(requestRepository.findByRootId(pending.getRootId())).thenReturn(List.of(pending));
        when(requestRepository.save(pending)).thenReturn(pending);
        when(requestMapper.toModel(pending)).thenReturn(new RequestModel());

        requestService.acceptRequest(pending.getId(), workerId);

        assertEquals(RequestState.ACCEPTED, pending.getState());
    }

    @Test
    @DisplayName("acceptRequest marca CONFLICT cuando otra solicitud del root ya fue aceptada")
    void aceptarGeneraConflictoConAceptada() {
        UUID workerId = UUID.randomUUID();
        Request pending = request(RequestState.PENDING, workerId, LocalDateTime.now());
        Request accepted = requestWithRoot(pending.getRootId(), RequestState.ACCEPTED, UUID.randomUUID());
        when(requestRepository.findById(pending.getId())).thenReturn(Optional.of(pending));
        when(requestRepository.findByRootId(pending.getRootId())).thenReturn(List.of(pending, accepted));
        when(requestRepository.save(pending)).thenReturn(pending);
        when(requestMapper.toModel(pending)).thenReturn(new RequestModel());

        requestService.acceptRequest(pending.getId(), workerId);

        assertEquals(RequestState.CONFLICT, pending.getState());
        assertEquals(RequestState.CONFLICT, accepted.getState());
        verify(requestRepository).saveAll(List.of(accepted));
        verify(requestRepository).save(pending);
    }

    @Test
    @DisplayName("acceptRequest tambien genera conflicto si otra solicitud ya estaba en CONFLICT")
    void aceptarGeneraConflictoConOtraEnConflicto() {
        UUID workerId = UUID.randomUUID();
        Request pending = request(RequestState.PENDING, workerId, LocalDateTime.now());
        Request conflict = requestWithRoot(pending.getRootId(), RequestState.CONFLICT, UUID.randomUUID());
        when(requestRepository.findById(pending.getId())).thenReturn(Optional.of(pending));
        when(requestRepository.findByRootId(pending.getRootId())).thenReturn(List.of(pending, conflict));
        when(requestRepository.save(pending)).thenReturn(pending);
        when(requestMapper.toModel(pending)).thenReturn(new RequestModel());

        requestService.acceptRequest(pending.getId(), workerId);

        assertEquals(RequestState.CONFLICT, pending.getState());
        assertEquals(RequestState.CONFLICT, conflict.getState());
    }

    @Test
    @DisplayName("acceptRequest ignora solicitudes del mismo root que no estan aceptadas ni en conflicto")
    void aceptarIgnoraOtrosEstadosDelRoot() {
        UUID workerId = UUID.randomUUID();
        Request pending = request(RequestState.PENDING, workerId, LocalDateTime.now());
        Request rejected = requestWithRoot(pending.getRootId(), RequestState.REJECTED, UUID.randomUUID());
        Request expired = requestWithRoot(pending.getRootId(), RequestState.EXPIRED, UUID.randomUUID());
        when(requestRepository.findById(pending.getId())).thenReturn(Optional.of(pending));
        when(requestRepository.findByRootId(pending.getRootId())).thenReturn(List.of(pending, rejected, expired));
        when(requestRepository.save(pending)).thenReturn(pending);
        when(requestMapper.toModel(pending)).thenReturn(new RequestModel());

        requestService.acceptRequest(pending.getId(), workerId);

        assertEquals(RequestState.ACCEPTED, pending.getState());
        assertEquals(RequestState.REJECTED, rejected.getState());
        assertEquals(RequestState.EXPIRED, expired.getState());
        verify(requestRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("acceptRequest de una solicitud ajena responde 404 sin revelar que existe")
    void aceptarSolicitudAjena() {
        Request pending = request(RequestState.PENDING, UUID.randomUUID(), LocalDateTime.now());
        when(requestRepository.findById(pending.getId())).thenReturn(Optional.of(pending));

        assertThrows(RecursoNoEncontradoException.class,
                () -> requestService.acceptRequest(pending.getId(), UUID.randomUUID()));
        assertEquals(RequestState.PENDING, pending.getState());
        verify(requestRepository, never()).save(any());
    }

    @Test
    @DisplayName("acceptRequest de una solicitud inexistente responde 404")
    void aceptarSolicitudInexistente() {
        UUID id = UUID.randomUUID();
        when(requestRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> requestService.acceptRequest(id, UUID.randomUUID()));
    }

    @Test
    @DisplayName("acceptRequest rechaza solicitudes que no estan en PENDING")
    void aceptarSolicitudYaRespondida() {
        UUID workerId = UUID.randomUUID();
        Request accepted = request(RequestState.ACCEPTED, workerId, LocalDateTime.now());
        when(requestRepository.findById(accepted.getId())).thenReturn(Optional.of(accepted));

        assertThrows(TransicionInvalidaException.class,
                () -> requestService.acceptRequest(accepted.getId(), workerId));
        verify(requestRepository, never()).save(any());
    }

    @Test
    @DisplayName("acceptRequest rechaza solicitudes con mas de 30 minutos de creadas")
    void aceptarSolicitudVencida() {
        UUID workerId = UUID.randomUUID();
        Request pending = request(RequestState.PENDING, workerId, LocalDateTime.now().minusMinutes(31));
        when(requestRepository.findById(pending.getId())).thenReturn(Optional.of(pending));

        assertThrows(TransicionInvalidaException.class,
                () -> requestService.acceptRequest(pending.getId(), workerId));
        assertEquals(RequestState.PENDING, pending.getState());
    }

    @Test
    @DisplayName("rejectRequest pasa de PENDING a REJECTED")
    void rechazarSolicitud() {
        UUID workerId = UUID.randomUUID();
        Request pending = request(RequestState.PENDING, workerId, LocalDateTime.now());
        RequestModel model = new RequestModel();
        when(requestRepository.findById(pending.getId())).thenReturn(Optional.of(pending));
        when(requestRepository.save(pending)).thenReturn(pending);
        when(requestMapper.toModel(pending)).thenReturn(model);

        RequestModel result = requestService.rejectRequest(pending.getId(), workerId);

        assertSame(model, result);
        assertEquals(RequestState.REJECTED, pending.getState());
        verify(requestRepository).save(pending);
    }

    @Test
    @DisplayName("rejectRequest de una solicitud ajena responde 404")
    void rechazarSolicitudAjena() {
        Request pending = request(RequestState.PENDING, UUID.randomUUID(), LocalDateTime.now());
        when(requestRepository.findById(pending.getId())).thenReturn(Optional.of(pending));

        assertThrows(RecursoNoEncontradoException.class,
                () -> requestService.rejectRequest(pending.getId(), UUID.randomUUID()));
    }

    @Test
    @DisplayName("rejectRequest de una solicitud ya respondida lanza 409")
    void rechazarSolicitudYaRespondida() {
        UUID workerId = UUID.randomUUID();
        Request rejected = request(RequestState.REJECTED, workerId, LocalDateTime.now());
        when(requestRepository.findById(rejected.getId())).thenReturn(Optional.of(rejected));

        assertThrows(TransicionInvalidaException.class,
                () -> requestService.rejectRequest(rejected.getId(), workerId));
    }

    @Test
    @DisplayName("rejectRequest de una solicitud vencida lanza 409")
    void rechazarSolicitudVencida() {
        UUID workerId = UUID.randomUUID();
        Request pending = request(RequestState.PENDING, workerId, LocalDateTime.now().minusHours(1));
        when(requestRepository.findById(pending.getId())).thenReturn(Optional.of(pending));

        assertThrows(TransicionInvalidaException.class,
                () -> requestService.rejectRequest(pending.getId(), workerId));
    }

    @Test
    @DisplayName("acceptConflict deja la elegida ACCEPTED y cancela las demas del root")
    void resolverConflicto() {
        UUID contractorId = UUID.randomUUID();
        Request chosen = request(RequestState.CONFLICT, UUID.randomUUID(), LocalDateTime.now());
        chosen.setContractorId(contractorId);
        Request pending = requestWithRoot(chosen.getRootId(), RequestState.PENDING, UUID.randomUUID());
        Request accepted = requestWithRoot(chosen.getRootId(), RequestState.ACCEPTED, UUID.randomUUID());
        Request conflict = requestWithRoot(chosen.getRootId(), RequestState.CONFLICT, UUID.randomUUID());
        Request deleted = requestWithRoot(chosen.getRootId(), RequestState.DELETED, UUID.randomUUID());
        Request expired = requestWithRoot(chosen.getRootId(), RequestState.EXPIRED, UUID.randomUUID());
        RequestModel model = new RequestModel();
        when(requestRepository.findById(chosen.getId())).thenReturn(Optional.of(chosen));
        when(requestRepository.findByRootId(chosen.getRootId()))
                .thenReturn(List.of(chosen, pending, accepted, conflict, deleted, expired));
        when(requestRepository.save(chosen)).thenReturn(chosen);
        when(requestMapper.toModel(chosen)).thenReturn(model);

        RequestModel result = requestService.acceptConflict(chosen.getId(), contractorId);

        assertSame(model, result);
        assertEquals(RequestState.ACCEPTED, chosen.getState());
        for (Request canceled : List.of(pending, accepted, conflict)) {
            assertEquals(RequestState.CANCELED, canceled.getState());
            assertEquals(CancellationState.COMPLETED, canceled.getCancellationState());
            assertNotNull(canceled.getCancellationDateTime());
        }
        assertEquals(RequestState.DELETED, deleted.getState());
        assertNull(deleted.getCancellationDateTime());
        assertEquals(RequestState.EXPIRED, expired.getState());
        assertNull(expired.getCancellationDateTime());
        verify(requestRepository).saveAll(List.of(pending, accepted, conflict));
        verify(requestRepository).save(chosen);
    }

    @Test
    @DisplayName("acceptConflict sin otras solicitudes solo acepta la elegida")
    void resolverConflictoSinOtrasSolicitudes() {
        UUID contractorId = UUID.randomUUID();
        Request chosen = request(RequestState.CONFLICT, UUID.randomUUID(), LocalDateTime.now());
        chosen.setContractorId(contractorId);
        when(requestRepository.findById(chosen.getId())).thenReturn(Optional.of(chosen));
        when(requestRepository.findByRootId(chosen.getRootId())).thenReturn(List.of(chosen));
        when(requestRepository.save(chosen)).thenReturn(chosen);
        when(requestMapper.toModel(chosen)).thenReturn(new RequestModel());

        requestService.acceptConflict(chosen.getId(), contractorId);

        assertEquals(RequestState.ACCEPTED, chosen.getState());
        verify(requestRepository).saveAll(List.of());
    }

    @Test
    @DisplayName("acceptConflict de un contratante que no es duenio responde 404")
    void resolverConflictoDeOtroContratante() {
        Request chosen = request(RequestState.CONFLICT, UUID.randomUUID(), LocalDateTime.now());
        when(requestRepository.findById(chosen.getId())).thenReturn(Optional.of(chosen));

        assertThrows(RecursoNoEncontradoException.class,
                () -> requestService.acceptConflict(chosen.getId(), UUID.randomUUID()));
        verify(requestRepository, never()).save(any());
    }

    @Test
    @DisplayName("acceptConflict de una solicitud que no esta en CONFLICT lanza 409")
    void resolverConflictoEnEstadoInvalido() {
        UUID contractorId = UUID.randomUUID();
        Request pending = request(RequestState.PENDING, UUID.randomUUID(), LocalDateTime.now());
        pending.setContractorId(contractorId);
        when(requestRepository.findById(pending.getId())).thenReturn(Optional.of(pending));

        assertThrows(TransicionInvalidaException.class,
                () -> requestService.acceptConflict(pending.getId(), contractorId));
        verify(requestRepository, never()).save(any());
    }

    @Test
    @DisplayName("acceptConflict de una solicitud inexistente responde 404")
    void resolverConflictoInexistente() {
        UUID id = UUID.randomUUID();
        when(requestRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> requestService.acceptConflict(id, UUID.randomUUID()));
    }

    private Request request(RequestState state, UUID workerId, LocalDateTime dateCreated) {
        return requestWithRoot(UUID.randomUUID(), state, workerId, dateCreated);
    }

    private Request requestWithRoot(UUID rootId, RequestState state, UUID workerId) {
        return requestWithRoot(rootId, state, workerId, LocalDateTime.now());
    }

    private Request requestWithRoot(UUID rootId, RequestState state, UUID workerId, LocalDateTime dateCreated) {
        Request request = new Request();
        request.setId(UUID.randomUUID());
        request.setRootId(rootId);
        request.setContractorId(UUID.randomUUID());
        request.setWorkerId(workerId);
        request.setDescription("Reparar una gotera");
        request.setNeighborhood("Niza Antigua");
        request.setExactAddress("Cra. 70a # 118-33");
        request.setScheduledDateTime(LocalDateTime.now().plusDays(2));
        request.setState(state);
        request.setDateCreated(dateCreated);
        return request;
    }
}
