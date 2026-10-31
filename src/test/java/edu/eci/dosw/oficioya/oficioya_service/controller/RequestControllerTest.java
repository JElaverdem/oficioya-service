package edu.eci.dosw.oficioya.oficioya_service.controller;

import static org.junit.jupiter.api.Assertions.assertSame;
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
    @DisplayName("GET /requests lista las solicitudes del usuario autenticado")
    void listaMisSolicitudes() {
        UUID userId = UUID.randomUUID();
        List<RequestModel> models = List.of(new RequestModel());
        when(requestService.listRequestsForUser(userId)).thenReturn(models);

        List<RequestModel> result = requestController.listMine(authentication(userId));

        assertSame(models, result);
        verify(requestService).listRequestsForUser(userId);
    }

    @Test
    @DisplayName("PATCH /requests/{id}/accept delega con el usuario autenticado")
    void aceptaSolicitud() {
        UUID userId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        RequestModel model = new RequestModel();
        when(requestService.acceptRequest(requestId, userId)).thenReturn(model);

        RequestModel result = requestController.accept(requestId, authentication(userId));

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

        RequestModel result = requestController.reject(requestId, authentication(userId));

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

        RequestModel result = requestController.acceptConflict(requestId, authentication(userId));

        assertSame(model, result);
        verify(requestService).acceptConflict(requestId, userId);
    }

    private Authentication authentication(UUID userId) {
        return new UsernamePasswordAuthenticationToken(userId.toString(), null);
    }
}
