package edu.eci.dosw.oficioya.oficioya_service.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.eci.dosw.oficioya.oficioya_service.dto.WorkerStatsResponse;
import edu.eci.dosw.oficioya.oficioya_service.service.RequestService;

@RestController
@RequestMapping("/api/v1/services")
public class ServiceStatsController {

    private final RequestService requestService;

    public ServiceStatsController(RequestService requestService) {
        this.requestService = requestService;
    }

    @GetMapping("/workers/{id}/stats")
    public WorkerStatsResponse getWorkerStats(@PathVariable UUID id) {
        return requestService.getWorkerStats(id);
    }
}