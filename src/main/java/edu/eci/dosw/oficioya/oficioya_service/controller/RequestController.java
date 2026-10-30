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

@RestController
@RequestMapping("/api/v1/requests")
public class RequestController {

    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PreAuthorize("hasAnyRole('CONTRATANTE', 'TRABAJADOR')")
    @GetMapping
    public List<RequestModel> listMine(Authentication authentication) {
        return requestService.listRequestsForUser(CurrentUser.id(authentication));
    }

    @PreAuthorize("hasRole('TRABAJADOR')")
    @PatchMapping("/{id}/accept")
    public RequestModel accept(@PathVariable UUID id, Authentication authentication) {
        return requestService.acceptRequest(id, CurrentUser.id(authentication));
    }

    @PreAuthorize("hasRole('TRABAJADOR')")
    @PatchMapping("/{id}/reject")
    public RequestModel reject(@PathVariable UUID id, Authentication authentication) {
        return requestService.rejectRequest(id, CurrentUser.id(authentication));
    }

    @PreAuthorize("hasRole('CONTRATANTE')")
    @PatchMapping("/{id}/accept-conflict")
    public RequestModel acceptConflict(@PathVariable UUID id, Authentication authentication) {
        return requestService.acceptConflict(id, CurrentUser.id(authentication));
    }
}
