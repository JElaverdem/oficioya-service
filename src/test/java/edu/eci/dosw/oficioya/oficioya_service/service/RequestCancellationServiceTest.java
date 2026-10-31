package edu.eci.dosw.oficioya.oficioya_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import edu.eci.dosw.oficioya.oficioya_service.entity.Request;
import edu.eci.dosw.oficioya.oficioya_service.model.CancellationState;
import edu.eci.dosw.oficioya.oficioya_service.model.RequestState;
import edu.eci.dosw.oficioya.oficioya_service.repository.RequestRepository;

/**
 * SNOY-20: ventana de 24 h de las cancelaciones.
 *
 * Las penalizaciones (bloqueo de una semana al contratante y -0,5 de reputación al trabajador)
 * todavía no existen en el servicio (SNOY-15). Hoy una cancelación tardía solo lanza una excepción
 * con un texto provisional; estas pruebas fijan ese comportamiento actual y habrá que ajustarlas
 * cuando se implementen las penalizaciones.
 */
@ExtendWith(MockitoExtension.class)
class RequestCancellationServiceTest {

    @Mock
    RequestRepository requestRepository;

    @InjectMocks
    RequestCancellationService service;

    // ------------------------------------------------------------ cancelByClient

    @Test
    @DisplayName("el contratante cancela una ACCEPTED con más de 24 h de anticipación")
    void contratanteCancelaATiempo() {
        UUID contratante = UUID.randomUUID();
        Request r = solicitud(contratante, UUID.randomUUID(), RequestState.ACCEPTED, LocalDateTime.now().plusHours(25));
        when(requestRepository.findById(r.getId())).thenReturn(Optional.of(r));

        LocalDateTime antes = LocalDateTime.now();
        service.cancelByClient(r.getId(), contratante);

        assertCancelada(r, antes);
        verify(requestRepository).save(r);
    }

    @Test
    @DisplayName("el contratante no puede cancelar una ACCEPTED a menos de 24 h")
    void contratanteCancelaTarde() {
        UUID contratante = UUID.randomUUID();
        Request r = solicitud(contratante, UUID.randomUUID(), RequestState.ACCEPTED, LocalDateTime.now().plusHours(23));
        when(requestRepository.findById(r.getId())).thenReturn(Optional.of(r));

        assertThrows(RuntimeException.class, () -> service.cancelByClient(r.getId(), contratante));

        assertEquals(RequestState.ACCEPTED, r.getState());
        assertNull(r.getCancellationState());
        verify(requestRepository, never()).save(any());
    }

    @Test
    @DisplayName("el contratante cancela una PENDING aunque falten menos de 24 h (la ventana aplica a ACCEPTED)")
    void contratanteCancelaPendienteCercana() {
        UUID contratante = UUID.randomUUID();
        Request r = solicitud(contratante, UUID.randomUUID(), RequestState.PENDING, LocalDateTime.now().plusHours(2));
        when(requestRepository.findById(r.getId())).thenReturn(Optional.of(r));

        LocalDateTime antes = LocalDateTime.now();
        service.cancelByClient(r.getId(), contratante);

        assertCancelada(r, antes);
    }

    @Test
    @DisplayName("el contratante cancela una ACCEPTED sin fecha programada")
    void contratanteCancelaSinFechaProgramada() {
        UUID contratante = UUID.randomUUID();
        Request r = solicitud(contratante, UUID.randomUUID(), RequestState.ACCEPTED, null);
        when(requestRepository.findById(r.getId())).thenReturn(Optional.of(r));

        service.cancelByClient(r.getId(), contratante);

        assertEquals(RequestState.CANCELED, r.getState());
    }

    @Test
    @DisplayName("solo el contratante dueño puede cancelar")
    void contratanteAjeno() {
        Request r = solicitud(UUID.randomUUID(), UUID.randomUUID(), RequestState.ACCEPTED, LocalDateTime.now().plusDays(3));
        when(requestRepository.findById(r.getId())).thenReturn(Optional.of(r));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.cancelByClient(r.getId(), UUID.randomUUID()));

        assertEquals("Solo el contratante puede cancelar esta solicitud", ex.getMessage());
        verify(requestRepository, never()).save(any());
    }

    @Test
    @DisplayName("el contratante no puede cancelar una solicitud COMPLETED")
    void contratanteCancelaCompletada() {
        UUID contratante = UUID.randomUUID();
        Request r = solicitud(contratante, UUID.randomUUID(), RequestState.COMPLETED, LocalDateTime.now().plusDays(3));
        when(requestRepository.findById(r.getId())).thenReturn(Optional.of(r));

        assertThrows(RuntimeException.class, () -> service.cancelByClient(r.getId(), contratante));

        assertEquals(RequestState.COMPLETED, r.getState());
        verify(requestRepository, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = RequestState.class, names = { "CANCELED", "DELETED" })
    @DisplayName("el contratante no puede cancelar una solicitud ya cancelada o eliminada")
    void contratanteCancelaYaCancelada(RequestState estado) {
        UUID contratante = UUID.randomUUID();
        Request r = solicitud(contratante, UUID.randomUUID(), estado, LocalDateTime.now().plusDays(3));
        when(requestRepository.findById(r.getId())).thenReturn(Optional.of(r));

        assertThrows(RuntimeException.class, () -> service.cancelByClient(r.getId(), contratante));

        assertEquals(estado, r.getState());
        verify(requestRepository, never()).save(any());
    }

    @Test
    @DisplayName("cancelar una solicitud inexistente como contratante lanza NoSuchElementException")
    void contratanteInexistente() {
        UUID id = UUID.randomUUID();
        when(requestRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.cancelByClient(id, UUID.randomUUID()));
    }

    // ------------------------------------------------------------ cancelByWorker

    @Test
    @DisplayName("el trabajador cancela una ACCEPTED con más de 24 h de anticipación")
    void trabajadorCancelaATiempo() {
        UUID trabajador = UUID.randomUUID();
        Request r = solicitud(UUID.randomUUID(), trabajador, RequestState.ACCEPTED, LocalDateTime.now().plusHours(30));
        when(requestRepository.findById(r.getId())).thenReturn(Optional.of(r));

        LocalDateTime antes = LocalDateTime.now();
        service.cancelByWorker(r.getId(), trabajador);

        assertCancelada(r, antes);
        verify(requestRepository).save(r);
    }

    @Test
    @DisplayName("el trabajador no puede cancelar una ACCEPTED a menos de 24 h")
    void trabajadorCancelaTarde() {
        UUID trabajador = UUID.randomUUID();
        Request r = solicitud(UUID.randomUUID(), trabajador, RequestState.ACCEPTED, LocalDateTime.now().plusHours(5));
        when(requestRepository.findById(r.getId())).thenReturn(Optional.of(r));

        assertThrows(RuntimeException.class, () -> service.cancelByWorker(r.getId(), trabajador));

        assertEquals(RequestState.ACCEPTED, r.getState());
        verify(requestRepository, never()).save(any());
    }

    @Test
    @DisplayName("el trabajador cancela una PENDING aunque falten menos de 24 h")
    void trabajadorCancelaPendienteCercana() {
        UUID trabajador = UUID.randomUUID();
        Request r = solicitud(UUID.randomUUID(), trabajador, RequestState.PENDING, LocalDateTime.now().plusHours(1));
        when(requestRepository.findById(r.getId())).thenReturn(Optional.of(r));

        LocalDateTime antes = LocalDateTime.now();
        service.cancelByWorker(r.getId(), trabajador);

        assertCancelada(r, antes);
    }

    @Test
    @DisplayName("solo el trabajador asignado puede cancelar")
    void trabajadorAjeno() {
        Request r = solicitud(UUID.randomUUID(), UUID.randomUUID(), RequestState.ACCEPTED, LocalDateTime.now().plusDays(3));
        when(requestRepository.findById(r.getId())).thenReturn(Optional.of(r));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.cancelByWorker(r.getId(), UUID.randomUUID()));

        assertEquals("Solo el trabajador asignado puede cancelar esta solicitud", ex.getMessage());
        verify(requestRepository, never()).save(any());
    }

    @Test
    @DisplayName("el trabajador no puede cancelar una solicitud COMPLETED")
    void trabajadorCancelaCompletada() {
        UUID trabajador = UUID.randomUUID();
        Request r = solicitud(UUID.randomUUID(), trabajador, RequestState.COMPLETED, LocalDateTime.now().plusDays(3));
        when(requestRepository.findById(r.getId())).thenReturn(Optional.of(r));

        assertThrows(RuntimeException.class, () -> service.cancelByWorker(r.getId(), trabajador));

        assertEquals(RequestState.COMPLETED, r.getState());
    }

    @ParameterizedTest
    @EnumSource(value = RequestState.class, names = { "CANCELED", "DELETED" })
    @DisplayName("el trabajador no puede cancelar una solicitud ya cancelada o eliminada")
    void trabajadorCancelaYaCancelada(RequestState estado) {
        UUID trabajador = UUID.randomUUID();
        Request r = solicitud(UUID.randomUUID(), trabajador, estado, LocalDateTime.now().plusDays(3));
        when(requestRepository.findById(r.getId())).thenReturn(Optional.of(r));

        assertThrows(RuntimeException.class, () -> service.cancelByWorker(r.getId(), trabajador));

        assertEquals(estado, r.getState());
        verify(requestRepository, never()).save(any());
    }

    @Test
    @DisplayName("el trabajador cancela una ACCEPTED sin fecha programada")
    void trabajadorCancelaSinFechaProgramada() {
        UUID trabajador = UUID.randomUUID();
        Request r = solicitud(UUID.randomUUID(), trabajador, RequestState.ACCEPTED, null);
        when(requestRepository.findById(r.getId())).thenReturn(Optional.of(r));

        service.cancelByWorker(r.getId(), trabajador);

        assertEquals(RequestState.CANCELED, r.getState());
    }

    @Test
    @DisplayName("cancelar una solicitud inexistente como trabajador lanza NoSuchElementException")
    void trabajadorInexistente() {
        UUID id = UUID.randomUUID();
        when(requestRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.cancelByWorker(id, UUID.randomUUID()));
    }

    // ------------------------------------------------------------ utilidades

    private void assertCancelada(Request r, LocalDateTime antes) {
        assertEquals(RequestState.CANCELED, r.getState());
        assertEquals(CancellationState.COMPLETED, r.getCancellationState());
        assertNotNull(r.getCancellationDateTime());
        assertFalse(r.getCancellationDateTime().isBefore(antes));
    }

    private Request solicitud(UUID contratante, UUID trabajador, RequestState estado, LocalDateTime programada) {
        Request r = new Request();
        r.setId(UUID.randomUUID());
        r.setRootId(UUID.randomUUID());
        r.setContractorId(contratante);
        r.setWorkerId(trabajador);
        r.setScheduledDateTime(programada);
        r.setState(estado);
        r.setDateCreated(LocalDateTime.now());
        return r;
    }
}
