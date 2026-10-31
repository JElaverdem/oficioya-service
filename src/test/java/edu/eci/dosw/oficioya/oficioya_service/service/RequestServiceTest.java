package edu.eci.dosw.oficioya.oficioya_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mapstruct.factory.Mappers;
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

/** SNOY-20: reglas de RequestService (ventana de 30 min, transiciones de estado y conflictos). */
@ExtendWith(MockitoExtension.class)
class RequestServiceTest {

    @Mock
    RequestRepository requestRepository;

    RequestService service;

    UUID contratante;
    UUID trabajador;
    UUID rootId;

    @BeforeEach
    void preparar() {
        service = new RequestService(requestRepository, Mappers.getMapper(RequestMapper.class));
        contratante = UUID.randomUUID();
        trabajador = UUID.randomUUID();
        rootId = UUID.randomUUID();
        // save devuelve lo mismo que recibe, como haría JPA
        lenient().when(requestRepository.save(any(Request.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    // ------------------------------------------------------------ listRequestsForUser

    @Test
    @DisplayName("listRequestsForUser convierte las solicitudes del usuario a modelo")
    void listarSolicitudesDelUsuario() {
        Request a = solicitud(RequestState.PENDING, LocalDateTime.now());
        Request b = solicitud(RequestState.ACCEPTED, LocalDateTime.now());
        when(requestRepository.findAllRequestsForUser(trabajador)).thenReturn(List.of(a, b));

        List<RequestModel> resultado = service.listRequestsForUser(trabajador);

        assertEquals(2, resultado.size());
        assertEquals(a.getId(), resultado.get(0).getId());
        assertEquals(RequestState.ACCEPTED, resultado.get(1).getState());
    }

    @Test
    @DisplayName("listRequestsForUser devuelve lista vacía si no hay solicitudes")
    void listarSinSolicitudes() {
        when(requestRepository.findAllRequestsForUser(trabajador)).thenReturn(List.of());

        assertTrue(service.listRequestsForUser(trabajador).isEmpty());
    }

    // ------------------------------------------------------------ acceptRequest

    @Test
    @DisplayName("aceptar sin otras aceptadas en el mismo rootId deja la solicitud en ACCEPTED")
    void aceptarSinConflicto() {
        Request r = solicitud(RequestState.PENDING, LocalDateTime.now());
        when(requestRepository.findById(r.getId())).thenReturn(Optional.of(r));
        when(requestRepository.findByRootId(rootId)).thenReturn(List.of(r));

        RequestModel resultado = service.acceptRequest(r.getId(), trabajador);

        assertEquals(RequestState.ACCEPTED, resultado.getState());
        verify(requestRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("aceptar cuando otra del mismo rootId ya está ACCEPTED pasa ambas a CONFLICT")
    void aceptarConOtraAceptadaGeneraConflicto() {
        Request nueva = solicitud(RequestState.PENDING, LocalDateTime.now());
        Request yaAceptada = solicitud(RequestState.ACCEPTED, LocalDateTime.now());
        when(requestRepository.findById(nueva.getId())).thenReturn(Optional.of(nueva));
        when(requestRepository.findByRootId(rootId)).thenReturn(List.of(nueva, yaAceptada));

        RequestModel resultado = service.acceptRequest(nueva.getId(), trabajador);

        assertEquals(RequestState.CONFLICT, resultado.getState());
        assertEquals(RequestState.CONFLICT, yaAceptada.getState());
        verify(requestRepository).saveAll(List.of(yaAceptada));
    }

    @Test
    @DisplayName("aceptar cuando otra del mismo rootId ya está en CONFLICT mantiene el conflicto")
    void aceptarConOtraEnConflicto() {
        Request nueva = solicitud(RequestState.PENDING, LocalDateTime.now());
        Request enConflicto = solicitud(RequestState.CONFLICT, LocalDateTime.now());
        when(requestRepository.findById(nueva.getId())).thenReturn(Optional.of(nueva));
        when(requestRepository.findByRootId(rootId)).thenReturn(List.of(nueva, enConflicto));

        RequestModel resultado = service.acceptRequest(nueva.getId(), trabajador);

        assertEquals(RequestState.CONFLICT, resultado.getState());
        assertEquals(RequestState.CONFLICT, enConflicto.getState());
    }

    @ParameterizedTest
    @EnumSource(value = RequestState.class, names = { "PENDING", "REJECTED", "CANCELED", "EXPIRED", "COMPLETED", "DELETED" })
    @DisplayName("aceptar ignora las del mismo rootId que no están ACCEPTED ni CONFLICT")
    void aceptarIgnoraOtrasEnOtrosEstados(RequestState otroEstado) {
        Request nueva = solicitud(RequestState.PENDING, LocalDateTime.now());
        Request otra = solicitud(otroEstado, LocalDateTime.now());
        when(requestRepository.findById(nueva.getId())).thenReturn(Optional.of(nueva));
        when(requestRepository.findByRootId(rootId)).thenReturn(List.of(nueva, otra));

        RequestModel resultado = service.acceptRequest(nueva.getId(), trabajador);

        assertEquals(RequestState.ACCEPTED, resultado.getState());
        assertEquals(otroEstado, otra.getState());
    }

    @Test
    @DisplayName("aceptar dentro de la ventana (29 min) es válido")
    void aceptarDentroDeLaVentana() {
        Request r = solicitud(RequestState.PENDING, LocalDateTime.now().minusMinutes(29));
        when(requestRepository.findById(r.getId())).thenReturn(Optional.of(r));
        when(requestRepository.findByRootId(rootId)).thenReturn(List.of(r));

        assertEquals(RequestState.ACCEPTED, service.acceptRequest(r.getId(), trabajador).getState());
    }

    @Test
    @DisplayName("aceptar una solicitud sin fecha de creación no aplica la ventana de 30 min")
    void aceptarSinFechaDeCreacion() {
        Request r = solicitud(RequestState.PENDING, null);
        when(requestRepository.findById(r.getId())).thenReturn(Optional.of(r));
        when(requestRepository.findByRootId(rootId)).thenReturn(List.of(r));

        assertEquals(RequestState.ACCEPTED, service.acceptRequest(r.getId(), trabajador).getState());
    }

    @Test
    @DisplayName("aceptar pasados los 30 min lanza TransicionInvalidaException y no guarda")
    void aceptarFueraDeVentana() {
        Request r = solicitud(RequestState.PENDING, LocalDateTime.now().minusMinutes(31));
        when(requestRepository.findById(r.getId())).thenReturn(Optional.of(r));

        TransicionInvalidaException ex = assertThrows(TransicionInvalidaException.class,
                () -> service.acceptRequest(r.getId(), trabajador));

        assertTrue(ex.getMessage().contains("30 minutos"));
        assertEquals(RequestState.PENDING, r.getState());
        verify(requestRepository, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = RequestState.class, mode = EnumSource.Mode.EXCLUDE, names = { "PENDING" })
    @DisplayName("aceptar una solicitud que no está PENDING lanza TransicionInvalidaException")
    void aceptarSiNoEstaPendiente(RequestState estado) {
        Request r = solicitud(estado, LocalDateTime.now());
        when(requestRepository.findById(r.getId())).thenReturn(Optional.of(r));

        assertThrows(TransicionInvalidaException.class, () -> service.acceptRequest(r.getId(), trabajador));

        assertEquals(estado, r.getState());
        verify(requestRepository, never()).save(any());
    }

    @Test
    @DisplayName("aceptar una solicitud asignada a otro trabajador responde como inexistente")
    void aceptarSolicitudAjena() {
        Request r = solicitud(RequestState.PENDING, LocalDateTime.now());
        when(requestRepository.findById(r.getId())).thenReturn(Optional.of(r));

        RecursoNoEncontradoException ex = assertThrows(RecursoNoEncontradoException.class,
                () -> service.acceptRequest(r.getId(), UUID.randomUUID()));

        assertEquals("Solicitud no encontrada", ex.getMessage());
        assertEquals(RequestState.PENDING, r.getState());
        verify(requestRepository, never()).save(any());
    }

    @Test
    @DisplayName("aceptar una solicitud inexistente lanza RecursoNoEncontradoException")
    void aceptarInexistente() {
        UUID id = UUID.randomUUID();
        when(requestRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.acceptRequest(id, trabajador));
    }

    // ------------------------------------------------------------ rejectRequest

    @Test
    @DisplayName("rechazar una solicitud PENDING dentro de la ventana la deja en REJECTED")
    void rechazarExitoso() {
        Request r = solicitud(RequestState.PENDING, LocalDateTime.now());
        when(requestRepository.findById(r.getId())).thenReturn(Optional.of(r));

        RequestModel resultado = service.rejectRequest(r.getId(), trabajador);

        assertEquals(RequestState.REJECTED, resultado.getState());
        verify(requestRepository).save(r);
    }

    @Test
    @DisplayName("rechazar pasados los 30 min lanza TransicionInvalidaException")
    void rechazarFueraDeVentana() {
        Request r = solicitud(RequestState.PENDING, LocalDateTime.now().minusMinutes(45));
        when(requestRepository.findById(r.getId())).thenReturn(Optional.of(r));

        assertThrows(TransicionInvalidaException.class, () -> service.rejectRequest(r.getId(), trabajador));

        assertEquals(RequestState.PENDING, r.getState());
    }

    @ParameterizedTest
    @EnumSource(value = RequestState.class, mode = EnumSource.Mode.EXCLUDE, names = { "PENDING" })
    @DisplayName("rechazar una solicitud que no está PENDING lanza TransicionInvalidaException")
    void rechazarSiNoEstaPendiente(RequestState estado) {
        Request r = solicitud(estado, LocalDateTime.now());
        when(requestRepository.findById(r.getId())).thenReturn(Optional.of(r));

        assertThrows(TransicionInvalidaException.class, () -> service.rejectRequest(r.getId(), trabajador));

        assertEquals(estado, r.getState());
    }

    @Test
    @DisplayName("rechazar una solicitud de otro trabajador responde como inexistente")
    void rechazarSolicitudAjena() {
        Request r = solicitud(RequestState.PENDING, LocalDateTime.now());
        when(requestRepository.findById(r.getId())).thenReturn(Optional.of(r));

        assertThrows(RecursoNoEncontradoException.class,
                () -> service.rejectRequest(r.getId(), UUID.randomUUID()));

        assertEquals(RequestState.PENDING, r.getState());
        verify(requestRepository, never()).save(any());
    }

    // ------------------------------------------------------------ acceptConflict

    @Test
    @DisplayName("resolver conflicto deja la elegida en ACCEPTED y cancela las demás sin penalización")
    void resolverConflicto() {
        Request elegida = solicitud(RequestState.CONFLICT, LocalDateTime.now());
        Request enConflicto = solicitud(RequestState.CONFLICT, LocalDateTime.now());
        Request pendiente = solicitud(RequestState.PENDING, LocalDateTime.now());
        Request aceptada = solicitud(RequestState.ACCEPTED, LocalDateTime.now());
        when(requestRepository.findById(elegida.getId())).thenReturn(Optional.of(elegida));
        when(requestRepository.findByRootId(rootId))
                .thenReturn(List.of(elegida, enConflicto, pendiente, aceptada));

        LocalDateTime antes = LocalDateTime.now();
        RequestModel resultado = service.acceptConflict(elegida.getId(), contratante);

        assertEquals(RequestState.ACCEPTED, resultado.getState());
        for (Request cancelada : List.of(enConflicto, pendiente, aceptada)) {
            assertEquals(RequestState.CANCELED, cancelada.getState());
            assertEquals(CancellationState.COMPLETED, cancelada.getCancellationState());
            assertNotNull(cancelada.getCancellationDateTime());
            assertFalse(cancelada.getCancellationDateTime().isBefore(antes));
        }
        // la elegida no se marca como cancelada
        assertNull(elegida.getCancellationState());
        verify(requestRepository).saveAll(List.of(enConflicto, pendiente, aceptada));
    }

    @ParameterizedTest
    @EnumSource(value = RequestState.class, names = { "REJECTED", "EXPIRED", "COMPLETED", "CANCELED", "DELETED" })
    @DisplayName("resolver conflicto no toca las solicitudes ya cerradas del mismo rootId")
    void resolverConflictoNoTocaLasCerradas(RequestState cerrado) {
        Request elegida = solicitud(RequestState.CONFLICT, LocalDateTime.now());
        Request otra = solicitud(cerrado, LocalDateTime.now());
        when(requestRepository.findById(elegida.getId())).thenReturn(Optional.of(elegida));
        when(requestRepository.findByRootId(rootId)).thenReturn(List.of(elegida, otra));

        service.acceptConflict(elegida.getId(), contratante);

        assertEquals(cerrado, otra.getState());
        assertNull(otra.getCancellationState());
    }

    @ParameterizedTest
    @EnumSource(value = RequestState.class, mode = EnumSource.Mode.EXCLUDE, names = { "CONFLICT" })
    @DisplayName("resolver conflicto sobre una solicitud que no está en CONFLICT lanza TransicionInvalidaException")
    void resolverConflictoSinConflicto(RequestState estado) {
        Request r = solicitud(estado, LocalDateTime.now());
        when(requestRepository.findById(r.getId())).thenReturn(Optional.of(r));

        assertThrows(TransicionInvalidaException.class, () -> service.acceptConflict(r.getId(), contratante));

        assertEquals(estado, r.getState());
        verify(requestRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("resolver el conflicto de otro contratante responde como inexistente")
    void resolverConflictoAjeno() {
        Request r = solicitud(RequestState.CONFLICT, LocalDateTime.now());
        when(requestRepository.findById(r.getId())).thenReturn(Optional.of(r));

        assertThrows(RecursoNoEncontradoException.class,
                () -> service.acceptConflict(r.getId(), UUID.randomUUID()));

        assertEquals(RequestState.CONFLICT, r.getState());
        verify(requestRepository, never()).save(any());
    }

    @Test
    @DisplayName("resolver el conflicto de una solicitud inexistente lanza RecursoNoEncontradoException")
    void resolverConflictoInexistente() {
        UUID id = UUID.randomUUID();
        when(requestRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.acceptConflict(id, contratante));
    }

    // ------------------------------------------------------------ utilidades

    private Request solicitud(RequestState estado, LocalDateTime creada) {
        Request r = new Request();
        r.setId(UUID.randomUUID());
        r.setRootId(rootId);
        r.setContractorId(contratante);
        r.setWorkerId(trabajador);
        r.setDescription("Arreglo de una fuga");
        r.setNeighborhood("Usaquén");
        r.setExactAddress("Calle 1 # 2-3");
        r.setScheduledDateTime(LocalDateTime.now().plusDays(3));
        r.setState(estado);
        r.setDateCreated(creada);
        return r;
    }
}
