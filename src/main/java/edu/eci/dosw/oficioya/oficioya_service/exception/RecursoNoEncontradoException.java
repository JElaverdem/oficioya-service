package edu.eci.dosw.oficioya.oficioya_service.exception;

public class RecursoNoEncontradoException extends RuntimeException {
    public static final String SOLICITUD_NO_ENCONTRADA = "Solicitud no encontrada";
    public static final String TRABAJADOR_NO_ENCONTRADO = "No se encontró el trabajador o trabajadores";
    public static final String CONTRATADOR_NO_ENCONTRADO = "No se encontró el tcontratador";
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
