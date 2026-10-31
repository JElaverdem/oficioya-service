package edu.eci.dosw.oficioya.oficioya_service.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.JwtException;

class JwtUtilTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef";
    private static final String ISSUER = "oficioya";

    private final JwtUtil jwtUtil = new JwtUtil(SECRET, ISSUER, 30);

    @Test
    @DisplayName("Genera un token valido y extrae usuario y rol")
    void generaYValidaToken() {
        UUID userId = UUID.randomUUID();

        String token = jwtUtil.generateToken(userId, "TRABAJADOR");

        assertTrue(jwtUtil.isTokenValid(token));
        assertEquals(userId, jwtUtil.extractUserId(token));
        assertEquals(userId.toString(), jwtUtil.extractUsername(token));
        assertEquals("TRABAJADOR", jwtUtil.extractRole(token));
    }

    @Test
    @DisplayName("Un token expirado no es valido")
    void tokenExpiradoNoEsValido() {
        JwtUtil expirado = new JwtUtil(SECRET, ISSUER, -1);
        String token = expirado.generateToken(UUID.randomUUID(), "CONTRATANTE");

        assertFalse(jwtUtil.isTokenValid(token));
        assertThrows(JwtException.class, () -> jwtUtil.extractUserId(token));
    }

    @Test
    @DisplayName("Un token firmado con otra clave no es valido")
    void firmaDeOtraClaveNoEsValida() {
        JwtUtil otraClave = new JwtUtil("abcdef0123456789abcdef0123456789", ISSUER, 30);
        String token = otraClave.generateToken(UUID.randomUUID(), "TRABAJADOR");

        assertFalse(jwtUtil.isTokenValid(token));
        assertThrows(JwtException.class, () -> jwtUtil.extractUserId(token));
    }

    @Test
    @DisplayName("Un token con otro emisor no es valido")
    void issuerDistintoNoEsValido() {
        JwtUtil otroEmisor = new JwtUtil(SECRET, "otro-servicio", 30);
        String token = otroEmisor.generateToken(UUID.randomUUID(), "TRABAJADOR");

        assertFalse(jwtUtil.isTokenValid(token));
        assertThrows(JwtException.class, () -> jwtUtil.extractUserId(token));
    }

    @Test
    @DisplayName("Un token malformado no es valido")
    void tokenMalformadoNoEsValido() {
        assertFalse(jwtUtil.isTokenValid("esto-no-es-un-jwt"));
        assertThrows(JwtException.class, () -> jwtUtil.extractUserId("esto-no-es-un-jwt"));
    }

    @Test
    @DisplayName("Un token alterado no es valido")
    void tokenAlteradoNoEsValido() {
        String token = jwtUtil.generateToken(UUID.randomUUID(), "CONTRATANTE");
        String alterado = token.substring(0, token.length() - 2) + "xx";

        assertFalse(jwtUtil.isTokenValid(alterado));
    }
}
