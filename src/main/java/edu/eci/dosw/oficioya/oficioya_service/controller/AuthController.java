package edu.eci.dosw.oficioya.oficioya_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.eci.dosw.oficioya.oficioya_service.dto.LoginRequest;
import edu.eci.dosw.oficioya.oficioya_service.dto.LoginResponse;
import edu.eci.dosw.oficioya.oficioya_service.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Autenticacion", description = "Login opcional. El servicio de solicitudes no exige token")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "Iniciar sesion (opcional)",
            description = "Devuelve un JWT con vigencia de 30 minutos. En esta version de laboratorio "
                    + "las credenciales se ignoran y el token se emite con rol CONTRATANTE. "
                    + "Los endpoints de /api/v1/requests no lo necesitan: pueden usar el header X-User-Id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token emitido"),
            @ApiResponse(responseCode = "400", description = "Correo o password invalidos")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }
}
