package edu.eci.dosw.oficioya.oficioya_service.security;

import java.util.UUID;

import org.springframework.security.core.Authentication;

/**
 * Punto único para obtener el id (UUID) del usuario autenticado.
 * El id viaja en el "sub" del JWT y JwtAuthFilter lo deja como nombre del Authentication.
 */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static UUID id(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
