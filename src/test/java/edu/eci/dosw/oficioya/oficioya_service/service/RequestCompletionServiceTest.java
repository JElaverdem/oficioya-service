package edu.eci.dosw.oficioya.oficioya_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import edu.eci.dosw.oficioya.oficioya_service.model.RequestState;
import edu.eci.dosw.oficioya.oficioya_service.repository.RequestRepository;

@ExtendWith(MockitoExtension.class)
class RequestCompletionServiceTest {

    @Mock
    private RequestRepository requestRepository;

    @InjectMocks
    private RequestCompletionService completionService;

    @Test
    @DisplayName("El trabajador asignado completa una solicitud ACCEPTED")
    void completaSolicitudAccepted() {
        Request request = request(RequestState.ACCEPTED);
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        completionService.completeRequest(request.getId(), request.getWorkerId());

        assertEquals(RequestState.COMPLETED, request.getState());
        verify(requestRepository).save(request);
    }

    @Test
    @DisplayName("Un trabajador distinto no puede completar la solicitud")
    void trabajadorAjenoNoCompleta() {
        Request request = request(RequestState.ACCEPTED);
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        assertThrows(RuntimeException.class,
                () -> completionService.completeRequest(request.getId(), UUID.randomUUID()));

        assertEquals(RequestState.ACCEPTED, request.getState());
        verify(requestRepository, never()).save(any());
    }

    @Test
    @DisplayName("No se puede completar una solicitud PENDING")
    void noCompletaPending() {
        Request request = request(RequestState.PENDING);
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        assertThrows(RuntimeException.class,
                () -> completionService.completeRequest(request.getId(), request.getWorkerId()));
    }

    @Test
    @DisplayName("No se puede completar una solicitud ya COMPLETED")
    void noCompletaCompletada() {
        Request request = request(RequestState.COMPLETED);
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        assertThrows(RuntimeException.class,
                () -> completionService.completeRequest(request.getId(), request.getWorkerId()));
    }

    @Test
    @DisplayName("No se puede completar una solicitud CANCELED")
    void noCompletaCancelada() {
        Request request = request(RequestState.CANCELED);
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        assertThrows(RuntimeException.class,
                () -> completionService.completeRequest(request.getId(), request.getWorkerId()));
    }

    @Test
    @DisplayName("No se puede completar una solicitud REJECTED")
    void noCompletaRechazada() {
        Request request = request(RequestState.REJECTED);
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        assertThrows(RuntimeException.class,
                () -> completionService.completeRequest(request.getId(), request.getWorkerId()));
    }

    @Test
    @DisplayName("Completar una solicitud inexistente lanza NoSuchElementException")
    void completarInexistente() {
        UUID id = UUID.randomUUID();
        when(requestRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> completionService.completeRequest(id, UUID.randomUUID()));
    }

    private Request request(RequestState state) {
        Request request = new Request();
        request.setId(UUID.randomUUID());
        request.setRootId(UUID.randomUUID());
        request.setContractorId(UUID.randomUUID());
        request.setWorkerId(UUID.randomUUID());
        request.setDescription("Reparar una gotera");
        request.setNeighborhood("Niza Antigua");
        request.setExactAddress("Cra. 70a # 118-33");
        request.setScheduledDateTime(LocalDateTime.now().plusDays(1));
        request.setState(state);
        request.setDateCreated(LocalDateTime.now());
        return request;
    }
}
