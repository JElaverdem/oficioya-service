package edu.eci.dosw.oficioya.oficioya_service.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import edu.eci.dosw.oficioya.oficioya_service.dto.PerfilAdmin;
import edu.eci.dosw.oficioya.oficioya_service.dto.PerfilPublico;
import edu.eci.dosw.oficioya.oficioya_service.entity.Usuario;
import edu.eci.dosw.oficioya.oficioya_service.exception.RecursoNoEncontradoException;
import edu.eci.dosw.oficioya.oficioya_service.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario porCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    public PerfilAdmin perfilAdmin(Long id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        return new PerfilAdmin(u.getId(), u.getNombre(), u.getCorreo(), u.getTelefono(),
                u.getOficioPrincipal(), u.getZona());
    }

    public java.util.List<PerfilAdmin> listarParaAdmin() {
        return usuarioRepository.findAll().stream()
                .map(u -> new PerfilAdmin(u.getId(), u.getNombre(), u.getCorreo(), u.getTelefono(),
                        u.getOficioPrincipal(), u.getZona()))
                .toList();
    }

    public PerfilPublico perfilPublico(Long id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        return new PerfilPublico(u.getId(), u.getNombre(), u.getOficioPrincipal(), u.getZona());
    }

    public PerfilAdmin perfilPropio() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth.getName();
        Usuario u = porCorreo(correo);
        return new PerfilAdmin(u.getId(), u.getNombre(), u.getCorreo(), u.getTelefono(),
                u.getOficioPrincipal(), u.getZona());
    }
}
