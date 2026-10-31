package edu.eci.dosw.oficioya.oficioya_service.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.MethodArgumentNotValidException;

import edu.eci.dosw.oficioya.oficioya_service.dto.ApiError;

class ApiExceptionHandlerTest {

    private static final String RUTA = "/api/v1/requests";

    private ApiExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new ApiExceptionHandler();
        request = new MockHttpServletRequest("GET", RUTA);
    }

    @Test
    @DisplayName("CorreoDuplicadoException responde 409")
    void correoDuplicado() {
        ResponseEntity<ApiError> response = handler.duplicado(new CorreoDuplicadoException("a@b.com"), request);

        assertError(response, 409, "Conflict");
    }

    @Test
    @DisplayName("RolNoPermitidoException responde 400")
    void rolNoPermitido() {
        ResponseEntity<ApiError> response = handler.rolNoPermitido(new RolNoPermitidoException("rol invalido"), request);

        assertError(response, 400, "Bad Request");
    }

    @Test
    @DisplayName("CredencialesInvalidasException responde 401")
    void credencialesInvalidas() {
        ResponseEntity<ApiError> response = handler.credencialesInvalidas(new CredencialesInvalidasException(), request);

        assertError(response, 401, "Unauthorized");
    }

    @Test
    @DisplayName("RecursoNoEncontradoException responde 404 con el mensaje de la excepcion")
    void recursoNoEncontrado() {
        ResponseEntity<ApiError> response =
                handler.recursoNoEncontrado(new RecursoNoEncontradoException("Solicitud no encontrada"), request);

        assertError(response, 404, "Not Found");
        assertEquals("Solicitud no encontrada", response.getBody().mensaje());
    }

    @Test
    @DisplayName("TransicionInvalidaException responde 409 con el mensaje de la excepcion")
    void transicionInvalida() {
        ResponseEntity<ApiError> response =
                handler.transicionInvalida(new TransicionInvalidaException("Solo PENDING"), request);

        assertError(response, 409, "Conflict");
        assertEquals("Solo PENDING", response.getBody().mensaje());
    }

    @Test
    @DisplayName("MethodArgumentNotValidException responde 400")
    void datosInvalidos() {
        ResponseEntity<ApiError> response =
                handler.datosInvalidos(mock(MethodArgumentNotValidException.class), request);

        assertError(response, 400, "Bad Request");
    }

    @Test
    @DisplayName("HttpMessageNotReadableException responde 400")
    void cuerpoIlegible() {
        ResponseEntity<ApiError> response =
                handler.cuerpoIlegible(mock(HttpMessageNotReadableException.class), request);

        assertError(response, 400, "Bad Request");
    }

    @Test
    @DisplayName("IllegalArgumentException responde 400 con el mensaje de la excepcion")
    void argumentoInvalido() {
        ResponseEntity<ApiError> response =
                handler.argumentoInvalido(new IllegalArgumentException("Falta el header X-User-Id"), request);

        assertError(response, 400, "Bad Request");
        assertEquals("Falta el header X-User-Id", response.getBody().mensaje());
    }

    private void assertError(ResponseEntity<ApiError> response, int status, String error) {
        assertEquals(status, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(status, response.getBody().status());
        assertEquals(error, response.getBody().error());
        assertEquals(RUTA, response.getBody().ruta());
        assertNotNull(response.getBody().timestamp());
    }
}
