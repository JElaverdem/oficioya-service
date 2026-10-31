package edu.eci.dosw.oficioya.oficioya_service.security;

import java.util.UUID;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

/**
 * Punto unico para obtener el id (UUID) del usuario de la peticion.
 *
 * El servicio no exige autenticacion: el usuario se toma del header X-User-Id.
 * Si ese header no viene, se usa el "sub" del JWT cuando hay un token valido.
 */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static UUID id(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalArgumentException("No hay usuario autenticado");
        }
        return UUID.fromString(authentication.getName());
    }

    /**
     * Resuelve el usuario de la peticion.
     *
     * @param authentication autenticacion opcional creada por JwtAuthFilter
     * @param headerUserId   UUID enviado en el header X-User-Id (tiene prioridad)
     * @return UUID del usuario
     * @throws IllegalArgumentException si no hay header ni token valido
     */
    public static UUID id(Authentication authentication, UUID headerUserId) {
        if (headerUserId != null) {
            return headerUserId;
        }
        if (authentication != null
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return id(authentication);
        }
        throw new IllegalArgumentException("Falta el header X-User-Id con el UUID del usuario");
    }
}
