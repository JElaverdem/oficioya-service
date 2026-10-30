package edu.eci.dosw.oficioya.oficioya_service.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import edu.eci.dosw.oficioya.oficioya_service.dto.LoginRequest;
import edu.eci.dosw.oficioya.oficioya_service.dto.LoginResponse;
import edu.eci.dosw.oficioya.oficioya_service.model.Rol;
import edu.eci.dosw.oficioya.oficioya_service.security.JwtUtil;

@Service
public class AuthService {

    // Rol del token de prueba. Cambiarlo a TRABAJADOR para probar manualmente aceptar/rechazar.
    private static final Rol ROL_PREDETERMINADO = Rol.CONTRATANTE;

    private final JwtUtil jwtUtil;

    public AuthService(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest req) {
        // TODO: la autenticación real (y el registro) debe ser una conexión con otro servicio
        // que todavía hay que definir cómo implementar. Mientras tanto se ignoran las credenciales
        // y siempre se devuelve un token válido, con un UUID de usuario nuevo, para poder hacer pruebas.
        UUID userId = UUID.randomUUID();
        String token = jwtUtil.generateToken(userId, ROL_PREDETERMINADO.name());
        return new LoginResponse("Bearer", token, 30 * 60);
    }
}
