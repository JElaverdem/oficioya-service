package edu.eci.dosw.oficioya.oficioya_service.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

class CurrentUserTest {

    @Test
    @DisplayName("Devuelve el UUID que viene en el nombre del Authentication")
    void devuelveUuidDelAuthentication() {
        UUID userId = UUID.randomUUID();
        Authentication authentication = new UsernamePasswordAuthenticationToken(userId.toString(), null);

        assertEquals(userId, CurrentUser.id(authentication));
    }

    @Test
    @DisplayName("Un nombre que no es UUID lanza IllegalArgumentException")
    void nombreInvalidoLanzaExcepcion() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("no-es-un-uuid", null);

        assertThrows(IllegalArgumentException.class, () -> CurrentUser.id(authentication));
    }
}
