package edu.eci.dosw.oficioya.oficioya_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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

/** SNOY-20: expiración de solicitudes PENDING que superan los 30 minutos. */
@ExtendWith(MockitoExtension.class)
class RequestExpirationServiceTest {

    @Mock
    RequestRepository requestRepository;

    @InjectMocks
    RequestExpirationService service;

    @Test
    @DisplayName("las solicitudes PENDING vencidas pasan a EXPIRED y se guardan")
    void expiraLasVencidas() {
        Request a = solicitud();
        Request b = solicitud();
        when(requestRepository.findByStateAndDateCreatedBefore(any(), any())).thenReturn(List.of(a, b));

        service.expirePendingRequests();

        assertEquals(RequestState.EXPIRED, a.getState());
        assertEquals(RequestState.EXPIRED, b.getState());
        verify(requestRepository).save(a);
        verify(requestRepository).save(b);
    }

    @Test
    @DisplayName("sin solicitudes vencidas no se guarda nada")
    void sinVencidas() {
        when(requestRepository.findByStateAndDateCreatedBefore(any(), any())).thenReturn(List.of());

        service.expirePendingRequests();

        verify(requestRepository, never()).save(any());
    }

    @Test
    @DisplayName("el corte es 30 minutos atrás y solo consulta solicitudes PENDING")
    void consultaConElCorteDe30Minutos() {
        when(requestRepository.findByStateAndDateCreatedBefore(any(), any())).thenReturn(List.of());
        ArgumentCaptor<LocalDateTime> corte = ArgumentCaptor.forClass(LocalDateTime.class);

        LocalDateTime antes = LocalDateTime.now().minusMinutes(30);
        service.expirePendingRequests();
        LocalDateTime despues = LocalDateTime.now().minusMinutes(30);

        verify(requestRepository).findByStateAndDateCreatedBefore(eq(RequestState.PENDING), corte.capture());
        assertFalse(corte.getValue().isBefore(antes));
        assertFalse(corte.getValue().isAfter(despues));
    }

    private Request solicitud() {
        Request r = new Request();
        r.setId(UUID.randomUUID());
        r.setState(RequestState.PENDING);
        r.setDateCreated(LocalDateTime.now().minusMinutes(40));
        return r;
    }
}
