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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import edu.eci.dosw.oficioya.oficioya_service.entity.Request;
import edu.eci.dosw.oficioya.oficioya_service.model.RequestState;
import edu.eci.dosw.oficioya.oficioya_service.repository.RequestRepository;

/** SNOY-20: completar una solicitud (solo el trabajador asignado y solo desde ACCEPTED). */
@ExtendWith(MockitoExtension.class)
class RequestCompletionServiceTest {

    @Mock
    RequestRepository requestRepository;

    @InjectMocks
    RequestCompletionService service;

    @Test
    @DisplayName("el trabajador asignado completa una solicitud ACCEPTED")
    void completarExitoso() {
        UUID trabajador = UUID.randomUUID();
        Request r = solicitud(trabajador, RequestState.ACCEPTED);
        when(requestRepository.findById(r.getId())).thenReturn(Optional.of(r));

        service.completeRequest(r.getId(), trabajador);

        assertEquals(RequestState.COMPLETED, r.getState());
        verify(requestRepository).save(r);
    }

    @Test
    @DisplayName("otro trabajador no puede completar la solicitud")
    void completarConOtroTrabajador() {
        Request r = solicitud(UUID.randomUUID(), RequestState.ACCEPTED);
        when(requestRepository.findById(r.getId())).thenReturn(Optional.of(r));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.completeRequest(r.getId(), UUID.randomUUID()));

        assertEquals("Solo el trabajador asignado puede completar esta solicitud", ex.getMessage());
        assertEquals(RequestState.ACCEPTED, r.getState());
        verify(requestRepository, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = RequestState.class, mode = EnumSource.Mode.EXCLUDE, names = { "ACCEPTED" })
    @DisplayName("solo se completan solicitudes ACCEPTED")
    void completarSoloDesdeAceptada(RequestState estado) {
        UUID trabajador = UUID.randomUUID();
        Request r = solicitud(trabajador, estado);
        when(requestRepository.findById(r.getId())).thenReturn(Optional.of(r));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.completeRequest(r.getId(), trabajador));

        assertEquals("Solo se pueden completar solicitudes en estado ACCEPTED", ex.getMessage());
        assertEquals(estado, r.getState());
        verify(requestRepository, never()).save(any());
    }

    @Test
    @DisplayName("completar una solicitud inexistente lanza NoSuchElementException")
    void completarInexistente() {
        UUID id = UUID.randomUUID();
        when(requestRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.completeRequest(id, UUID.randomUUID()));
    }

    private Request solicitud(UUID trabajador, RequestState estado) {
        Request r = new Request();
        r.setId(UUID.randomUUID());
        r.setRootId(UUID.randomUUID());
        r.setContractorId(UUID.randomUUID());
        r.setWorkerId(trabajador);
        r.setScheduledDateTime(LocalDateTime.now().plusDays(2));
        r.setState(estado);
        r.setDateCreated(LocalDateTime.now());
        return r;
    }
}
