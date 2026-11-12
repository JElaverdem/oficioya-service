package edu.eci.dosw.oficioya.oficioya_service.controller;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.eci.dosw.oficioya.oficioya_service.dto.ServiceReportResponse;
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

    @GetMapping("/reports")
    public ServiceReportResponse getReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return requestService.getReport(from, to);
    }
}