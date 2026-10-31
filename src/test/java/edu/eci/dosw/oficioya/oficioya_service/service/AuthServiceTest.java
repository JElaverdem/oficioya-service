package edu.eci.dosw.oficioya.oficioya_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.eci.dosw.oficioya.oficioya_service.dto.LoginRequest;
import edu.eci.dosw.oficioya.oficioya_service.dto.LoginResponse;
import edu.eci.dosw.oficioya.oficioya_service.security.JwtUtil;

class AuthServiceTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef";

    private final JwtUtil jwtUtil = new JwtUtil(SECRET, "oficioya", 30);
    private final AuthService authService = new AuthService(jwtUtil);

    @Test
    @DisplayName("El login devuelve un Bearer valido con rol CONTRATANTE")
    void loginDevuelveTokenValidoConRolContratante() {
        LoginResponse response = authService.login(new LoginRequest("usuario@oficioya.com", "secreto"));

        assertEquals("Bearer", response.tipo());
        assertEquals(1800, response.expiraEn());
        assertNotNull(response.accessToken());
        assertTrue(jwtUtil.isTokenValid(response.accessToken()));
        assertEquals("CONTRATANTE", jwtUtil.extractRole(response.accessToken()));
        assertNotNull(jwtUtil.extractUserId(response.accessToken()));
    }

    @Test
    @DisplayName("Cada login genera un usuario diferente")
    void cadaLoginGeneraUsuarioDiferente() {
        LoginResponse primero = authService.login(new LoginRequest("usuario@oficioya.com", "secreto"));
        LoginResponse segundo = authService.login(new LoginRequest("usuario@oficioya.com", "secreto"));

        assertNotEquals(
                jwtUtil.extractUserId(primero.accessToken()),
                jwtUtil.extractUserId(segundo.accessToken()));
    }

    @Test
    @DisplayName("El login actual ignora las credenciales (comportamiento temporal)")
    void loginIgnoraCredenciales() {
        LoginResponse response = authService.login(new LoginRequest("cualquiera@oficioya.com", "otra-clave"));

        assertTrue(jwtUtil.isTokenValid(response.accessToken()));
    }
}
