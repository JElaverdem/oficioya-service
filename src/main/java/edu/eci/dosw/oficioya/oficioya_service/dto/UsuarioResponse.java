package edu.eci.dosw.oficioya.oficioya_service.dto;

import edu.eci.dosw.oficioya.oficioya_service.model.Rol;

public record UsuarioResponse(
        Long id,
        String nombre,
        String correo,
        Rol rol) {
}
