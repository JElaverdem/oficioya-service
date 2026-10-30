package edu.eci.dosw.oficioya.oficioya_service.service;

import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import edu.eci.dosw.oficioya.oficioya_service.dto.LoginRequest;
import edu.eci.dosw.oficioya.oficioya_service.dto.LoginResponse;
import edu.eci.dosw.oficioya.oficioya_service.dto.RegistroRequest;
import edu.eci.dosw.oficioya.oficioya_service.dto.UsuarioResponse;
import edu.eci.dosw.oficioya.oficioya_service.entity.Usuario;
import edu.eci.dosw.oficioya.oficioya_service.exception.CorreoDuplicadoException;
import edu.eci.dosw.oficioya.oficioya_service.exception.CredencialesInvalidasException;
import edu.eci.dosw.oficioya.oficioya_service.exception.RolNoPermitidoException;
import edu.eci.dosw.oficioya.oficioya_service.model.Rol;
import edu.eci.dosw.oficioya.oficioya_service.repository.UsuarioRepository;
import edu.eci.dosw.oficioya.oficioya_service.security.JwtUtil;

@Service
public class AuthService {

    private static final Set<Rol> ROLES_PUBLICOS = Set.of(Rol.TRABAJADOR, Rol.CONTRATANTE);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public UsuarioResponse registrar(RegistroRequest req) {
        if (!ROLES_PUBLICOS.contains(req.rol())) {
            throw new RolNoPermitidoException("Rol no permitido en el registro");
        }
        String correo = req.correo().trim().toLowerCase();
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new CorreoDuplicadoException(correo);
        }
        Usuario u = new Usuario();
        u.setNombre(req.nombre());
        u.setCorreo(correo);
        u.setTelefono(req.telefono());
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        u.setRol(req.rol());
        u.setOficioPrincipal(req.oficioPrincipal());
        u.setZona(req.zona());
        u.setFechaCreacion(LocalDateTime.now());
        Usuario saved = usuarioRepository.save(u);
        return new UsuarioResponse(saved.getId(), saved.getNombre(), saved.getCorreo(), saved.getRol());
    }

    public LoginResponse login(LoginRequest req) {
        String correo = req.correo().trim().toLowerCase();
        Usuario u = usuarioRepository.findByCorreo(correo)
                .filter(x -> passwordEncoder.matches(req.password(), x.getPasswordHash()))
                .orElseThrow(CredencialesInvalidasException::new);
        String token = jwtUtil.generateToken(u.getCorreo(), u.getRol().name());
        return new LoginResponse("Bearer", token, 30 * 60);
    }
}
