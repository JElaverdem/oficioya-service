package edu.eci.dosw.oficioya.oficioya_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciales para iniciar sesion")
public record LoginRequest(
        @Schema(description = "Correo electronico del usuario", example = "usuario@oficioya.com")
        @NotBlank @Email String correo,

        @Schema(description = "Password del usuario", example = "secreto")
        @NotBlank String password) {
}
