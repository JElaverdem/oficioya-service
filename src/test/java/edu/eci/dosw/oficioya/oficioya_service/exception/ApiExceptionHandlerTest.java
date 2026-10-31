package edu.eci.dosw.oficioya.oficioya_service.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import edu.eci.dosw.oficioya.oficioya_service.dto.ApiError;

/** SNOY-20: cada excepción de negocio se traduce al código HTTP y al cuerpo JSON esperados. */
class ApiExceptionHandlerTest {

    private static final String RUTA = "/api/v1/requests/123/accept";

    private final ApiExceptionHandler handler = new ApiExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest("PATCH", RUTA);

    @Test
    @DisplayName("TransicionInvalidaException -> 409 con el mensaje de la excepción")
    void transicionInvalida() {
        ResponseEntity<ApiError> r = handler.transicionInvalida(
                new TransicionInvalidaException("Solo se pueden responder solicitudes en estado PENDING"), request);

        verificar(r, HttpStatus.CONFLICT, "Conflict", "Solo se pueden responder solicitudes en estado PENDING");
    }

    @Test
    @DisplayName("RecursoNoEncontradoException -> 404 con el mensaje de la excepción")
    void recursoNoEncontrado() {
        ResponseEntity<ApiError> r = handler.recursoNoEncontrado(
                new RecursoNoEncontradoException("Solicitud no encontrada"), request);

        verificar(r, HttpStatus.NOT_FOUND, "Not Found", "Solicitud no encontrada");
    }

    @Test
    @DisplayName("CorreoDuplicadoException -> 409")
    void correoDuplicado() {
        ResponseEntity<ApiError> r = handler.duplicado(new CorreoDuplicadoException("a@b.com"), request);

        verificar(r, HttpStatus.CONFLICT, "Conflict", "El correo ya está registrado");
    }

    @Test
    @DisplayName("RolNoPermitidoException -> 400 con el mensaje de la excepción")
    void rolNoPermitido() {
        ResponseEntity<ApiError> r = handler.rolNoPermitido(new RolNoPermitidoException("Rol no permitido"), request);

        verificar(r, HttpStatus.BAD_REQUEST, "Bad Request", "Rol no permitido");
    }

    @Test
    @DisplayName("CredencialesInvalidasException -> 401 sin detalles")
    void credencialesInvalidas() {
        ResponseEntity<ApiError> r = handler.credencialesInvalidas(new CredencialesInvalidasException(), request);

        verificar(r, HttpStatus.UNAUTHORIZED, "Unauthorized", "Credenciales inválidas");
    }

    @Test
    @DisplayName("datos inválidos -> 400 con mensaje genérico (no filtra detalles internos)")
    void datosInvalidos() {
        // El manejador no usa la excepción, solo la ruta de la petición.
        ResponseEntity<ApiError> r = handler.datosInvalidos(null, request);

        verificar(r, HttpStatus.BAD_REQUEST, "Bad Request", "Datos inválidos");
    }

    @Test
    @DisplayName("cuerpo ilegible -> 400 con mensaje genérico")
    void cuerpoIlegible() {
        ResponseEntity<ApiError> r = handler.cuerpoIlegible(null, request);

        verificar(r, HttpStatus.BAD_REQUEST, "Bad Request", "Cuerpo de la petición inválido");
    }

    private void verificar(ResponseEntity<ApiError> respuesta, HttpStatus estado, String error, String mensaje) {
        assertEquals(estado, respuesta.getStatusCode());
        ApiError cuerpo = respuesta.getBody();
        assertNotNull(cuerpo);
        assertEquals(estado.value(), cuerpo.status());
        assertEquals(error, cuerpo.error());
        assertTrue(cuerpo.mensaje().contains(mensaje));
        assertEquals(RUTA, cuerpo.ruta());
        assertNotNull(cuerpo.timestamp());
    }
}
