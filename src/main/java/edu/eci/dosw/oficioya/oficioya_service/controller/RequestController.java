package edu.eci.dosw.oficioya.oficioya_service.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.eci.dosw.oficioya.oficioya_service.model.RequestModel;
import edu.eci.dosw.oficioya.oficioya_service.security.CurrentUser;
import edu.eci.dosw.oficioya.oficioya_service.service.RequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Solicitudes", description = "Consulta y respuesta de solicitudes de servicio")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/requests")
public class RequestController {

    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @Operation(
            summary = "Listar mis solicitudes",
            description = "Devuelve las solicitudes donde el usuario autenticado es contratante o trabajador. "
                    + "Las solicitudes con estado DELETED no se incluyen.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de solicitudes"),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido o expirado")
    })
    @PreAuthorize("hasAnyRole('CONTRATANTE', 'TRABAJADOR')")
    @GetMapping
    public List<RequestModel> listMine(@Parameter(hidden = true) Authentication authentication) {
        return requestService.listRequestsForUser(CurrentUser.id(authentication));
    }

    @Operation(
            summary = "Aceptar una solicitud",
            description = "El trabajador asignado acepta una solicitud PENDING dentro de los 30 minutos. "
                    + "Si otra solicitud del mismo root ya estaba aceptada o en conflicto, ambas pasan a CONFLICT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud aceptada o marcada en conflicto"),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido o expirado"),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol TRABAJADOR"),
            @ApiResponse(responseCode = "404", description = "La solicitud no existe o no esta asignada al trabajador"),
            @ApiResponse(responseCode = "409", description = "La solicitud no esta PENDING o vencieron los 30 minutos")
    })
    @PreAuthorize("hasRole('TRABAJADOR')")
    @PatchMapping("/{id}/accept")
    public RequestModel accept(
            @Parameter(description = "Id de la solicitud", required = true) @PathVariable UUID id,
            @Parameter(hidden = true) Authentication authentication) {
        return requestService.acceptRequest(id, CurrentUser.id(authentication));
    }

    @Operation(
            summary = "Rechazar una solicitud",
            description = "El trabajador asignado rechaza una solicitud PENDING dentro de los 30 minutos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitud rechazada"),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido o expirado"),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol TRABAJADOR"),
            @ApiResponse(responseCode = "404", description = "La solicitud no existe o no esta asignada al trabajador"),
            @ApiResponse(responseCode = "409", description = "La solicitud no esta PENDING o vencieron los 30 minutos")
    })
    @PreAuthorize("hasRole('TRABAJADOR')")
    @PatchMapping("/{id}/reject")
    public RequestModel reject(
            @Parameter(description = "Id de la solicitud", required = true) @PathVariable UUID id,
            @Parameter(hidden = true) Authentication authentication) {
        return requestService.rejectRequest(id, CurrentUser.id(authentication));
    }

    @Operation(
            summary = "Resolver un conflicto",
            description = "El contratante duenio elige una solicitud en estado CONFLICT. "
                    + "La elegida pasa a ACCEPTED y las demas del mismo root se cancelan sin penalizacion.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conflicto resuelto"),
            @ApiResponse(responseCode = "401", description = "Token ausente, invalido o expirado"),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol CONTRATANTE"),
            @ApiResponse(responseCode = "404", description = "La solicitud no existe o no pertenece al contratante"),
            @ApiResponse(responseCode = "409", description = "La solicitud no esta en estado CONFLICT")
    })
    @PreAuthorize("hasRole('CONTRATANTE')")
    @PatchMapping("/{id}/accept-conflict")
    public RequestModel acceptConflict(
            @Parameter(description = "Id de la solicitud elegida", required = true) @PathVariable UUID id,
            @Parameter(hidden = true) Authentication authentication) {
        return requestService.acceptConflict(id, CurrentUser.id(authentication));
    }
}
