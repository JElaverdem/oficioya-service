package edu.eci.dosw.oficioya.oficioya_service.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import edu.eci.dosw.oficioya.oficioya_service.entity.Request;
import edu.eci.dosw.oficioya.oficioya_service.model.CancellationState;
import edu.eci.dosw.oficioya.oficioya_service.model.RequestState;

@DataJpaTest
@ActiveProfiles("test")
class RequestRepositoryQueriesTest {

    @Autowired
    private RequestRepository requestRepository;

    private UUID contractorId;
    private UUID workerId;

    @BeforeEach
    void setUp() {
        requestRepository.deleteAll();
        contractorId = UUID.randomUUID();
        workerId = UUID.randomUUID();
    }

    @Test
    @DisplayName("findByContractorId devuelve solo las solicitudes del contratante")
    void findByContractorId() {
        Request own = request(contractorId, UUID.randomUUID(), RequestState.PENDING, LocalDateTime.now());
        Request other = request(UUID.randomUUID(), UUID.randomUUID(), RequestState.PENDING, LocalDateTime.now());
        requestRepository.saveAll(List.of(own, other));

        List<Request> result = requestRepository.findByContractorId(contractorId);

        assertEquals(1, result.size());
        assertEquals(own.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("findByWorkerId devuelve solo las solicitudes del trabajador")
    void findByWorkerId() {
        Request own = request(UUID.randomUUID(), workerId, RequestState.PENDING, LocalDateTime.now());
        Request other = request(UUID.randomUUID(), UUID.randomUUID(), RequestState.PENDING, LocalDateTime.now());
        requestRepository.saveAll(List.of(own, other));

        List<Request> result = requestRepository.findByWorkerId(workerId);

        assertEquals(1, result.size());
        assertEquals(own.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("findByWorkerIdAndState filtra por trabajador y estado")
    void findByWorkerIdAndState() {
        Request pending = request(UUID.randomUUID(), workerId, RequestState.PENDING, LocalDateTime.now());
        Request accepted = request(UUID.randomUUID(), workerId, RequestState.ACCEPTED, LocalDateTime.now());
        Request otherWorker = request(UUID.randomUUID(), UUID.randomUUID(), RequestState.PENDING, LocalDateTime.now());
        requestRepository.saveAll(List.of(pending, accepted, otherWorker));

        List<Request> result = requestRepository.findByWorkerIdAndState(workerId, RequestState.PENDING);

        assertEquals(1, result.size());
        assertEquals(pending.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("findByRootIdAndState filtra por root y estado")
    void findByRootIdAndState() {
        UUID rootId = UUID.randomUUID();
        Request accepted = request(rootId, UUID.randomUUID(), UUID.randomUUID(), RequestState.ACCEPTED, LocalDateTime.now());
        Request rejected = request(rootId, UUID.randomUUID(), UUID.randomUUID(), RequestState.REJECTED, LocalDateTime.now());
        Request otherRoot = request(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), RequestState.ACCEPTED, LocalDateTime.now());
        requestRepository.saveAll(List.of(accepted, rejected, otherRoot));

        List<Request> result = requestRepository.findByRootIdAndState(rootId, RequestState.ACCEPTED);

        assertEquals(1, result.size());
        assertEquals(accepted.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("findByStateAndDateCreatedBefore solo devuelve PENDING vencidas")
    void findByStateAndDateCreatedBefore() {
        LocalDateTime now = LocalDateTime.now();
        Request oldPending = request(contractorId, workerId, RequestState.PENDING, now.minusMinutes(31));
        Request newPending = request(contractorId, workerId, RequestState.PENDING, now.minusMinutes(5));
        Request oldAccepted = request(contractorId, workerId, RequestState.ACCEPTED, now.minusMinutes(40));
        requestRepository.saveAll(List.of(oldPending, newPending, oldAccepted));

        List<Request> result = requestRepository.findByStateAndDateCreatedBefore(
                RequestState.PENDING, now.minusMinutes(30));

        assertEquals(1, result.size());
        assertEquals(oldPending.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("findAllRequestsForUser incluye las solicitudes donde es contratante o trabajador")
    void findAllRequestsForUserConAmbosRoles() {
        Request asContractor = request(contractorId, UUID.randomUUID(), RequestState.PENDING, LocalDateTime.now());
        Request asWorker = request(UUID.randomUUID(), contractorId, RequestState.ACCEPTED, LocalDateTime.now());
        Request other = request(UUID.randomUUID(), UUID.randomUUID(), RequestState.PENDING, LocalDateTime.now());
        requestRepository.saveAll(List.of(asContractor, asWorker, other));

        List<Request> result = requestRepository.findAllRequestsForUser(contractorId);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(request -> request.getId().equals(asContractor.getId())));
        assertTrue(result.stream().anyMatch(request -> request.getId().equals(asWorker.getId())));
    }

    @Test
    @DisplayName("findAllRequestsForUser excluye las solicitudes DELETED")
    void findAllRequestsForUserExcluyeEliminadas() {
        Request active = request(contractorId, workerId, RequestState.PENDING, LocalDateTime.now());
        Request deleted = request(contractorId, workerId, RequestState.DELETED, LocalDateTime.now());
        requestRepository.saveAll(List.of(active, deleted));

        List<Request> result = requestRepository.findAllRequestsForUser(contractorId);

        assertEquals(1, result.size());
        assertEquals(active.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("findAllRequestsForUserAndStateNot excluye el estado indicado")
    void findAllRequestsForUserAndStateNot() {
        Request accepted = request(contractorId, workerId, RequestState.ACCEPTED, LocalDateTime.now());
        Request rejected = request(contractorId, workerId, RequestState.REJECTED, LocalDateTime.now());
        requestRepository.saveAll(List.of(accepted, rejected));

        List<Request> result = requestRepository.findAllRequestsForUserAndStateNot(
                contractorId, RequestState.REJECTED);

        assertEquals(1, result.size());
        assertEquals(accepted.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("findContractorHasCancelledWorker encuentra una cancelacion IN_PROGRESS")
    void encuentraCancelacionEnProgreso() {
        Request canceled = request(contractorId, workerId, RequestState.CANCELED, LocalDateTime.now().minusDays(1));
        canceled.setCancellationState(CancellationState.IN_PROGRESS);
        canceled.setCancellationDateTime(LocalDateTime.now());
        requestRepository.save(canceled);

        Optional<Request> result = requestRepository.findContractorHasCancelledWorker(contractorId, workerId);

        assertTrue(result.isPresent());
        assertEquals(canceled.getId(), result.get().getId());
    }

    @Test
    @DisplayName("findContractorHasCancelledWorker ignora cancelaciones COMPLETED")
    void ignoraCancelacionCompletada() {
        Request canceled = request(contractorId, workerId, RequestState.CANCELED, LocalDateTime.now().minusDays(1));
        canceled.setCancellationState(CancellationState.COMPLETED);
        canceled.setCancellationDateTime(LocalDateTime.now());
        requestRepository.save(canceled);

        assertFalse(requestRepository.findContractorHasCancelledWorker(contractorId, workerId).isPresent());
    }

    @Test
    @DisplayName("findContractorHasCancelledWorker ignora otras parejas contratante/trabajador")
    void ignoraOtraPareja() {
        Request canceled = request(UUID.randomUUID(), UUID.randomUUID(), RequestState.CANCELED, LocalDateTime.now());
        canceled.setCancellationState(CancellationState.IN_PROGRESS);
        canceled.setCancellationDateTime(LocalDateTime.now());
        requestRepository.save(canceled);

        assertFalse(requestRepository.findContractorHasCancelledWorker(contractorId, workerId).isPresent());
    }

    @Test
    @DisplayName("findContractorHasCancelledWorker excluye solicitudes DELETED")
    void ignoraCancelacionEliminada() {
        Request canceled = request(contractorId, workerId, RequestState.DELETED, LocalDateTime.now());
        canceled.setCancellationState(CancellationState.IN_PROGRESS);
        canceled.setCancellationDateTime(LocalDateTime.now());
        requestRepository.save(canceled);

        assertFalse(requestRepository.findContractorHasCancelledWorker(contractorId, workerId).isPresent());
    }

    @Test
    @DisplayName("findContractorHasCancelledWorker exige fecha de cancelacion")
    void exigeFechaDeCancelacion() {
        Request canceled = request(contractorId, workerId, RequestState.CANCELED, LocalDateTime.now());
        canceled.setCancellationState(CancellationState.IN_PROGRESS);
        canceled.setCancellationDateTime(null);
        requestRepository.save(canceled);

        assertFalse(requestRepository.findContractorHasCancelledWorker(contractorId, workerId).isPresent());
    }

    @Test
    @DisplayName("specialDeleteById hace eliminacion logica y devuelve 1")
    void eliminacionLogica() {
        Request request = request(contractorId, workerId, RequestState.ACCEPTED, LocalDateTime.now());
        requestRepository.save(request);

        int updated = requestRepository.specialDeleteById(request.getId());

        assertEquals(1, updated);
        assertFalse(requestRepository.findAllRequestsForUser(contractorId).stream()
                .anyMatch(found -> found.getId().equals(request.getId())));
    }

    @Test
    @DisplayName("specialDeleteById devuelve 0 cuando la solicitud no existe")
    void eliminacionLogicaInexistente() {
        assertEquals(0, requestRepository.specialDeleteById(UUID.randomUUID()));
    }

    @Test
    @DisplayName("findByContractorIdOrWorkerId no duplica cuando el usuario tiene ambos roles")
    void noDuplicaCuandoEsAmbosRoles() {
        Request asContractor = request(contractorId, UUID.randomUUID(), RequestState.PENDING, LocalDateTime.now());
        Request asWorker = request(UUID.randomUUID(), contractorId, RequestState.PENDING, LocalDateTime.now());
        Request other = request(UUID.randomUUID(), UUID.randomUUID(), RequestState.PENDING, LocalDateTime.now());
        requestRepository.saveAll(List.of(asContractor, asWorker, other));

        List<Request> result = requestRepository.findByContractorIdOrWorkerId(contractorId, contractorId);

        assertEquals(2, result.size());
    }

    private Request request(UUID contractor, UUID worker, RequestState state, LocalDateTime dateCreated) {
        return request(UUID.randomUUID(), contractor, worker, state, dateCreated);
    }

    private Request request(UUID rootId, UUID contractor, UUID worker, RequestState state, LocalDateTime dateCreated) {
        Request request = new Request();
        request.setId(UUID.randomUUID());
        request.setRootId(rootId);
        request.setContractorId(contractor);
        request.setWorkerId(worker);
        request.setDescription("Reparar una gotera");
        request.setNeighborhood("Niza Antigua");
        request.setExactAddress("Cra. 70a # 118-33");
        request.setScheduledDateTime(LocalDateTime.now().plusDays(2));
        request.setState(state);
        request.setDateCreated(dateCreated);
        return request;
    }
}
