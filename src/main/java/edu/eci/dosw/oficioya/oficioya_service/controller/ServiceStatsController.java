package edu.eci.dosw.oficioya.oficioya_service.controller;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.eci.dosw.oficioya.oficioya_service.dto.ServiceReportResponse;
import edu.eci.dosw.oficioya.oficioya_service.dto.WorkerStatsResponse;
import edu.eci.dosw.oficioya.oficioya_service.service.RequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/services")
@Tag(name="Services", description = "API to control the get the reports")
public class ServiceStatsController {

    private final RequestService requestService;

    public ServiceStatsController(RequestService requestService) {
        this.requestService = requestService;
    }

    @GetMapping("/workers/{id}/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary="Get stats of a worker", description = "Gives the id, the number of works completed, money earned and his top services")
    public WorkerStatsResponse getWorkerStats(@PathVariable UUID id) {
        return requestService.getWorkerStats(id);
    }

    @GetMapping("/reports")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary="Get requests organized made in a defined time", description = "Gets the number of requests made in that slot of time and the requests separated by status, neighborhood and hour")
    public ServiceReportResponse getReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return requestService.getReport(from, to);
    }
}