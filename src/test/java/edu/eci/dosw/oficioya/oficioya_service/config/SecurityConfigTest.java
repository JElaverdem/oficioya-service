package edu.eci.dosw.oficioya.oficioya_service.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.PrintWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import edu.eci.dosw.oficioya.oficioya_service.security.JwtAuthFilter;
import tools.jackson.databind.ObjectMapper;

class SecurityConfigTest {

    private ObjectMapper objectMapper;
    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        objectMapper = mock(ObjectMapper.class);
        securityConfig = new SecurityConfig(mock(JwtAuthFilter.class), objectMapper);
    }

    @Test
    @DisplayName("El entry point responde 401 en JSON")
    void entryPointResponde401() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/requests");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthenticationEntryPoint entryPoint = securityConfig.authenticationEntryPoint();

        entryPoint.commence(request, response, mock(AuthenticationException.class));

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));
        assertEquals("UTF-8", response.getCharacterEncoding());
        verify(objectMapper).writeValue(any(PrintWriter.class), any());
    }

    @Test
    @DisplayName("El access denied handler responde 403 en JSON")
    void accessDeniedResponde403() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/admin");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AccessDeniedHandler handler = securityConfig.accessDeniedHandler();

        handler.handle(request, response, new AccessDeniedException("denegado"));

        assertEquals(403, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));
        assertEquals("UTF-8", response.getCharacterEncoding());
        verify(objectMapper).writeValue(any(PrintWriter.class), any());
    }

    @Test
    @DisplayName("El password encoder usa BCrypt")
    void passwordEncoderUsaBcrypt() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String hash = encoder.encode("secreto");

        assertTrue(encoder.matches("secreto", hash));
        assertTrue(hash.startsWith("$2"));
    }

    @Test
    @DisplayName("El ObjectMapper escribe el cuerpo del error en el writer")
    void entryPointUsaElWriter() throws Exception {
        SecurityConfig config = new SecurityConfig(mock(JwtAuthFilter.class), new ObjectMapper());
        MockHttpServletResponse response = new MockHttpServletResponse();

        config.authenticationEntryPoint().commence(
                new MockHttpServletRequest("GET", "/api/v1/requests"),
                response,
                mock(AuthenticationException.class));

        String body = response.getContentAsString();
        assertTrue(body.contains("\"status\":401"));
        assertTrue(body.contains("\"error\":\"Unauthorized\""));
        assertTrue(body.contains("/api/v1/requests"));
    }

    @Test
    @DisplayName("El access denied handler escribe el cuerpo del error en el writer")
    void accessDeniedUsaElWriter() throws Exception {
        SecurityConfig config = new SecurityConfig(mock(JwtAuthFilter.class), new ObjectMapper());
        MockHttpServletResponse response = new MockHttpServletResponse();

        config.accessDeniedHandler().handle(
                new MockHttpServletRequest("GET", "/api/v1/admin"),
                response,
                new AccessDeniedException("denegado"));

        String body = response.getContentAsString();
        assertTrue(body.contains("\"status\":403"));
        assertTrue(body.contains("\"error\":\"Forbidden\""));
        assertTrue(body.contains("/api/v1/admin"));
    }
}
