package edu.eci.dosw.oficioya.oficioya_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import edu.eci.dosw.oficioya.oficioya_service.entity.Request;
import edu.eci.dosw.oficioya.oficioya_service.model.CancellationState;
import edu.eci.dosw.oficioya.oficioya_service.model.RequestState;
import edu.eci.dosw.oficioya.oficioya_service.repository.RequestRepository;

@ExtendWith(MockitoExtension.class)
class RequestCancellationServiceTest {

    @Mock
    private RequestRepository requestRepository;

    @InjectMocks
    private RequestCancellationService cancellationService;

    @Test
    @DisplayName("El contratante cancela su solicitud PENDING sin penalizacion")
    void contratanteCancelaPending() {
        Request request = request(RequestState.PENDING, LocalDateTime.now().plusDays(3));
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        cancellationService.cancelByClient(request.getId(), request.getContractorId());

        assertCanceled(request);
        verify(requestRepository).save(request);
    }

    @Test
    @DisplayName("El contratante cancela una solicitud ACCEPTED con mas de 24 horas de anticipacion")
    void contratanteCancelaAcceptedConAnticipacion() {
        Request request = request(RequestState.ACCEPTED, LocalDateTime.now().plusHours(48));
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        cancellationService.cancelByClient(request.getId(), request.getContractorId());

        assertCanceled(request);
    }

    @Test
    @DisplayName("El contratante no puede cancelar una solicitud ACCEPTED con menos de 24 horas")
    void contratanteNoCancelaAcceptedCerca() {
        Request request = request(RequestState.ACCEPTED, LocalDateTime.now().plusHours(2));
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        assertThrows(RuntimeException.class,
                () -> cancellationService.cancelByClient(request.getId(), request.getContractorId()));

        assertEquals(RequestState.ACCEPTED, request.getState());
        verify(requestRepository, never()).save(any());
    }

    @Test
    @DisplayName("Una solicitud ACCEPTED sin fecha programada se puede cancelar")
    void contratanteCancelaAcceptedSinFecha() {
        Request request = request(RequestState.ACCEPTED, null);
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        cancellationService.cancelByClient(request.getId(), request.getContractorId());

        assertCanceled(request);
    }

    @Test
    @DisplayName("El contratante cancela una solicitud CONFLICT")
    void contratanteCancelaConflict() {
        Request request = request(RequestState.CONFLICT, LocalDateTime.now().plusDays(1));
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        cancellationService.cancelByClient(request.getId(), request.getContractorId());

        assertCanceled(request);
    }

    @Test
    @DisplayName("No se puede cancelar una solicitud COMPLETED")
    void noCancelaCompletada() {
        Request request = request(RequestState.COMPLETED, LocalDateTime.now().plusDays(1));
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        assertThrows(RuntimeException.class,
                () -> cancellationService.cancelByClient(request.getId(), request.getContractorId()));

        assertEquals(RequestState.COMPLETED, request.getState());
        verify(requestRepository, never()).save(any());
    }

    @Test
    @DisplayName("No se puede cancelar una solicitud ya CANCELED")
    void noCancelaYaCancelada() {
        Request request = request(RequestState.CANCELED, LocalDateTime.now().plusDays(1));
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        assertThrows(RuntimeException.class,
                () -> cancellationService.cancelByClient(request.getId(), request.getContractorId()));
    }

    @Test
    @DisplayName("No se puede cancelar una solicitud DELETED")
    void noCancelaEliminada() {
        Request request = request(RequestState.DELETED, LocalDateTime.now().plusDays(1));
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        assertThrows(RuntimeException.class,
                () -> cancellationService.cancelByClient(request.getId(), request.getContractorId()));
    }

    @Test
    @DisplayName("Un usuario que no es el contratante no puede cancelar")
    void contratanteAjenoNoCancela() {
        Request request = request(RequestState.PENDING, LocalDateTime.now().plusDays(1));
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        assertThrows(RuntimeException.class,
                () -> cancellationService.cancelByClient(request.getId(), UUID.randomUUID()));
    }

    @Test
    @DisplayName("Cancelar una solicitud inexistente lanza NoSuchElementException")
    void cancelarInexistente() {
        UUID id = UUID.randomUUID();
        when(requestRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> cancellationService.cancelByClient(id, UUID.randomUUID()));
    }

    @Test
    @DisplayName("El trabajador asignado cancela una solicitud PENDING")
    void trabajadorCancelaPending() {
        Request request = request(RequestState.PENDING, LocalDateTime.now().plusDays(3));
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        cancellationService.cancelByWorker(request.getId(), request.getWorkerId());

        assertCanceled(request);
        verify(requestRepository).save(request);
    }

    @Test
    @DisplayName("El trabajador cancela una solicitud ACCEPTED con anticipacion")
    void trabajadorCancelaAcceptedConAnticipacion() {
        Request request = request(RequestState.ACCEPTED, LocalDateTime.now().plusHours(25));
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        cancellationService.cancelByWorker(request.getId(), request.getWorkerId());

        assertCanceled(request);
    }

    @Test
    @DisplayName("El trabajador no puede cancelar una solicitud ACCEPTED con menos de 24 horas")
    void trabajadorNoCancelaAcceptedCerca() {
        Request request = request(RequestState.ACCEPTED, LocalDateTime.now().plusHours(1));
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        assertThrows(RuntimeException.class,
                () -> cancellationService.cancelByWorker(request.getId(), request.getWorkerId()));
    }

    @Test
    @DisplayName("Un trabajador que no esta asignado no puede cancelar")
    void trabajadorAjenoNoCancela() {
        Request request = request(RequestState.PENDING, LocalDateTime.now().plusDays(1));
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        assertThrows(RuntimeException.class,
                () -> cancellationService.cancelByWorker(request.getId(), UUID.randomUUID()));
    }

    @Test
    @DisplayName("El trabajador no puede cancelar una solicitud COMPLETED")
    void trabajadorNoCancelaCompletada() {
        Request request = request(RequestState.COMPLETED, LocalDateTime.now().plusDays(1));
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        assertThrows(RuntimeException.class,
                () -> cancellationService.cancelByWorker(request.getId(), request.getWorkerId()));
    }

    @Test
    @DisplayName("El trabajador no puede cancelar una solicitud ya CANCELED")
    void trabajadorNoCancelaYaCancelada() {
        Request request = request(RequestState.CANCELED, LocalDateTime.now().plusDays(1));
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        assertThrows(RuntimeException.class,
                () -> cancellationService.cancelByWorker(request.getId(), request.getWorkerId()));
    }

    @Test
    @DisplayName("El trabajador no puede cancelar una solicitud DELETED")
    void trabajadorNoCancelaEliminada() {
        Request request = request(RequestState.DELETED, LocalDateTime.now().plusDays(1));
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        assertThrows(RuntimeException.class,
                () -> cancellationService.cancelByWorker(request.getId(), request.getWorkerId()));
    }

    @Test
    @DisplayName("Cancelar como trabajador una solicitud inexistente lanza NoSuchElementException")
    void trabajadorCancelaInexistente() {
        UUID id = UUID.randomUUID();
        when(requestRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> cancellationService.cancelByWorker(id, UUID.randomUUID()));
    }

    private void assertCanceled(Request request) {
        assertEquals(RequestState.CANCELED, request.getState());
        assertEquals(CancellationState.COMPLETED, request.getCancellationState());
        assertNotNull(request.getCancellationDateTime());
    }

    private Request request(RequestState state, LocalDateTime scheduledDateTime) {
        Request request = new Request();
        request.setId(UUID.randomUUID());
        request.setRootId(UUID.randomUUID());
        request.setContractorId(UUID.randomUUID());
        request.setWorkerId(UUID.randomUUID());
        request.setDescription("Reparar una gotera");
        request.setNeighborhood("Niza Antigua");
        request.setExactAddress("Cra. 70a # 118-33");
        request.setScheduledDateTime(scheduledDateTime);
        request.setState(state);
        request.setDateCreated(LocalDateTime.now());
        assertNull(request.getCancellationState());
        return request;
    }
}
