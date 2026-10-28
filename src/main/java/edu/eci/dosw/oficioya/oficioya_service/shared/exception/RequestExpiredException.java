package edu.eci.dosw.oficioya.oficioya_service.shared.exception;

public class RequestExpiredException extends RuntimeException {
    public RequestExpiredException(String message) {
        super(message);
    }

    public RequestExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
