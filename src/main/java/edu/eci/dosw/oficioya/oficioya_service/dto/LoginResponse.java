package edu.eci.dosw.oficioya.oficioya_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta del inicio de sesion")
public record LoginResponse(
        @Schema(description = "Tipo de token", example = "Bearer")
        String tipo,

        @Schema(description = "JWT para enviar en el header Authorization", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "Vigencia del token en segundos", example = "1800")
        long expiraEn) {
}
