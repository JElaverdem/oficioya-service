package edu.eci.dosw.oficioya.oficioya_service.exception;

public class RequestSinWorkersException extends RuntimeException {
    public RequestSinWorkersException() {
        super("Lista de workers vacía");
    }
}
