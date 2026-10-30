package edu.eci.dosw.oficioya.oficioya_service.dto;

public record LoginResponse(
        String tipo,
        String accessToken,
        long expiraEn) {
}
