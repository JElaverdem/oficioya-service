package edu.eci.dosw.oficioya.oficioya_service.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ModelEnumsTest {

    @Test
    @DisplayName("Rol contiene los tres roles del sistema")
    void rolesDelSistema() {
        assertEquals(3, Rol.values().length);
        assertEquals(Rol.CONTRATANTE, Rol.valueOf("CONTRATANTE"));
        assertEquals(Rol.TRABAJADOR, Rol.valueOf("TRABAJADOR"));
        assertEquals(Rol.ADMIN, Rol.valueOf("ADMIN"));
    }

    @Test
    @DisplayName("RequestState contiene todos los estados de una solicitud")
    void estadosDeSolicitud() {
        assertEquals(8, RequestState.values().length);
        for (RequestState state : RequestState.values()) {
            assertNotNull(RequestState.valueOf(state.name()));
        }
    }

    @Test
    @DisplayName("CancellationState contiene los estados de cancelacion")
    void estadosDeCancelacion() {
        assertEquals(2, CancellationState.values().length);
        assertEquals(CancellationState.IN_PROGRESS, CancellationState.valueOf("IN_PROGRESS"));
        assertEquals(CancellationState.COMPLETED, CancellationState.valueOf("COMPLETED"));
    }
}
