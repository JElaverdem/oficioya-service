package edu.eci.dosw.oficioya.oficioya_service.dto;

public record PerfilAdmin(
        Long id,
        String nombre,
        String correo,
        String telefono,
        String oficioPrincipal,
        String zona) {
}
