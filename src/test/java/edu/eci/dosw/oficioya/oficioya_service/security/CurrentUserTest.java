package edu.eci.dosw.oficioya.oficioya_service.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

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

    @Test
    @DisplayName("Sin Authentication lanza IllegalArgumentException")
    void sinAuthenticationLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> CurrentUser.id(null));
    }

    @Test
    @DisplayName("El header X-User-Id tiene prioridad sobre el Authentication")
    void elHeaderTienePrioridad() {
        UUID delToken = UUID.randomUUID();
        UUID delHeader = UUID.randomUUID();

        assertEquals(delHeader, CurrentUser.id(authentication(delToken), delHeader));
    }

    @Test
    @DisplayName("Sin token se usa el header X-User-Id")
    void sinTokenUsaElHeader() {
        UUID delHeader = UUID.randomUUID();

        assertEquals(delHeader, CurrentUser.id(null, delHeader));
    }

    @Test
    @DisplayName("Un token real se usa cuando no hay header")
    void sinHeaderUsaElToken() {
        UUID delToken = UUID.randomUUID();

        assertEquals(delToken, CurrentUser.id(authentication(delToken), null));
    }

    @Test
    @DisplayName("La autenticacion anonima no cuenta como usuario")
    void autenticacionAnonimaNoCuenta() {
        UUID delHeader = UUID.randomUUID();

        assertEquals(delHeader, CurrentUser.id(anonymous(), delHeader));
        assertThrows(IllegalArgumentException.class, () -> CurrentUser.id(anonymous(), null));
    }

    private Authentication authentication(UUID userId) {
        return new UsernamePasswordAuthenticationToken(userId.toString(), null);
    }

    private Authentication anonymous() {
        return new AnonymousAuthenticationToken(
                "anon-key", "anonymousUser", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
    }
}
