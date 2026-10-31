package edu.eci.dosw.oficioya.oficioya_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import edu.eci.dosw.oficioya.oficioya_service.entity.Request;
import edu.eci.dosw.oficioya.oficioya_service.model.RequestState;
import edu.eci.dosw.oficioya.oficioya_service.repository.RequestRepository;

@ExtendWith(MockitoExtension.class)
class RequestExpirationServiceTest {

    @Mock
    private RequestRepository requestRepository;

    @InjectMocks
    private RequestExpirationService expirationService;

    @Test
    @DisplayName("Marca como EXPIRED las solicitudes PENDING devueltas por el repositorio")
    void expiraPendientesVencidas() {
        Request first = request(RequestState.PENDING);
        Request second = request(RequestState.PENDING);
        when(requestRepository.findByStateAndDateCreatedBefore(eq(RequestState.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(first, second));

        expirationService.expirePendingRequests();

        assertEquals(RequestState.EXPIRED, first.getState());
        assertEquals(RequestState.EXPIRED, second.getState());
        verify(requestRepository).save(first);
        verify(requestRepository).save(second);
    }

    @Test
    @DisplayName("Consulta PENDING con un limite de 30 minutos hacia atras")
    void usaElLimiteDeTreintaMinutos() {
        LocalDateTime before = LocalDateTime.now().minusMinutes(30);
        when(requestRepository.findByStateAndDateCreatedBefore(eq(RequestState.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of());

        expirationService.expirePendingRequests();

        LocalDateTime after = LocalDateTime.now().minusMinutes(30);
        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(requestRepository).findByStateAndDateCreatedBefore(eq(RequestState.PENDING), captor.capture());
        LocalDateTime limit = captor.getValue();
        assertTrue(!limit.isBefore(before) && !limit.isAfter(after));
    }

    @Test
    @DisplayName("Sin solicitudes vencidas no guarda nada")
    void sinPendientesNoGuarda() {
        when(requestRepository.findByStateAndDateCreatedBefore(eq(RequestState.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of());

        expirationService.expirePendingRequests();

        verify(requestRepository, never()).save(any());
    }

    @Test
    @DisplayName("No modifica solicitudes que el repositorio no devuelve como vencidas")
    void noModificaAceptadas() {
        Request accepted = request(RequestState.ACCEPTED);
        when(requestRepository.findByStateAndDateCreatedBefore(eq(RequestState.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of());

        expirationService.expirePendingRequests();

        assertEquals(RequestState.ACCEPTED, accepted.getState());
        verify(requestRepository, times(0)).save(accepted);
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
        request.setDateCreated(LocalDateTime.now().minusHours(1));
        return request;
    }
}
