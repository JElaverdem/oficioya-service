package edu.eci.dosw.oficioya.oficioya_service.exception;

public class RecursosAjenosException extends RuntimeException {
    public RecursosAjenosException() {
        super("Recursos no pertenecientes al usuario");
    }
}
