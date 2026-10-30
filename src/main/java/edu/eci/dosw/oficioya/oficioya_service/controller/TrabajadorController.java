package edu.eci.dosw.oficioya.oficioya_service.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.eci.dosw.oficioya.oficioya_service.dto.PerfilPublico;
import edu.eci.dosw.oficioya.oficioya_service.service.UsuarioService;

@RestController
@RequestMapping("/api/v1/trabajadores")
public class TrabajadorController {

    private final UsuarioService usuarioService;
    private final edu.eci.dosw.oficioya.oficioya_service.repository.UsuarioRepository usuarioRepository;

    public TrabajadorController(UsuarioService usuarioService,
                                edu.eci.dosw.oficioya.oficioya_service.repository.UsuarioRepository usuarioRepository) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public List<PerfilPublico> listar() {
        return usuarioRepository.findAll().stream()
                .filter(u -> u.getRol() == edu.eci.dosw.oficioya.oficioya_service.model.Rol.TRABAJADOR)
                .map(u -> usuarioService.perfilPublico(u.getId()))
                .toList();
    }

    @GetMapping("/{id}")
    public PerfilPublico obtener(@PathVariable Long id) {
        return usuarioService.perfilPublico(id);
    }
}
