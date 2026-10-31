package edu.eci.dosw.oficioya.oficioya_service.controller;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import edu.eci.dosw.oficioya.oficioya_service.model.RequestModel;
import edu.eci.dosw.oficioya.oficioya_service.service.RequestService;

@ExtendWith(MockitoExtension.class)
class RequestControllerTest {

    @Mock
    private RequestService requestService;

    @InjectMocks
    private RequestController requestController;

    @Test
    @DisplayName("GET /requests lista las solicitudes del usuario del token")
    void listaMisSolicitudes() {
        UUID userId = UUID.randomUUID();
        List<RequestModel> models = List.of(new RequestModel());
        when(requestService.listRequestsForUser(userId)).thenReturn(models);

        List<RequestModel> result = requestController.listMine(authentication(userId), null);

        assertSame(models, result);
        verify(requestService).listRequestsForUser(userId);
    }

    @Test
    @DisplayName("GET /requests usa el header X-User-Id cuando no hay token")
    void listaConHeaderSinToken() {
        UUID userId = UUID.randomUUID();
        List<RequestModel> models = List.of();
        when(requestService.listRequestsForUser(userId)).thenReturn(models);

        List<RequestModel> result = requestController.listMine(null, userId);

        assertSame(models, result);
        verify(requestService).listRequestsForUser(userId);
    }

    @Test
    @DisplayName("El header X-User-Id tiene prioridad sobre el usuario del token")
    void elHeaderTienePrioridad() {
        UUID delToken = UUID.randomUUID();
        UUID delHeader = UUID.randomUUID();
        when(requestService.listRequestsForUser(delHeader)).thenReturn(List.of());

        requestController.listMine(authentication(delToken), delHeader);

        verify(requestService).listRequestsForUser(delHeader);
    }

    @Test
    @DisplayName("Sin header ni token responde error de argumento")
    void sinUsuarioLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> requestController.listMine(null, null));
    }

    @Test
    @DisplayName("PATCH /requests/{id}/accept delega con el usuario autenticado")
    void aceptaSolicitud() {
        UUID userId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        RequestModel model = new RequestModel();
        when(requestService.acceptRequest(requestId, userId)).thenReturn(model);

        RequestModel result = requestController.accept(requestId, authentication(userId), null);

        assertSame(model, result);
        verify(requestService).acceptRequest(requestId, userId);
    }

    @Test
    @DisplayName("PATCH /requests/{id}/reject delega con el usuario autenticado")
    void rechazaSolicitud() {
        UUID userId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        RequestModel model = new RequestModel();
        when(requestService.rejectRequest(requestId, userId)).thenReturn(model);

        RequestModel result = requestController.reject(requestId, authentication(userId), null);

        assertSame(model, result);
        verify(requestService).rejectRequest(requestId, userId);
    }

    @Test
    @DisplayName("PATCH /requests/{id}/accept-conflict delega con el usuario autenticado")
    void resuelveConflicto() {
        UUID userId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        RequestModel model = new RequestModel();
        when(requestService.acceptConflict(requestId, userId)).thenReturn(model);

        RequestModel result = requestController.acceptConflict(requestId, authentication(userId), null);

        assertSame(model, result);
        verify(requestService).acceptConflict(requestId, userId);
    }

    @Test
    @DisplayName("Los PATCH usan el header X-User-Id cuando no hay token")
    void accionesConHeader() {
        UUID userId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        RequestModel model = new RequestModel();
        when(requestService.acceptRequest(requestId, userId)).thenReturn(model);
        when(requestService.rejectRequest(requestId, userId)).thenReturn(model);
        when(requestService.acceptConflict(requestId, userId)).thenReturn(model);

        assertSame(model, requestController.accept(requestId, null, userId));
        assertSame(model, requestController.reject(requestId, null, userId));
        assertSame(model, requestController.acceptConflict(requestId, null, userId));
    }

    private Authentication authentication(UUID userId) {
        return new UsernamePasswordAuthenticationToken(userId.toString(), null);
    }
}
