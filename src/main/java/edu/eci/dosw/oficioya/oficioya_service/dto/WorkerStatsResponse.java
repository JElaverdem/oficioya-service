package edu.eci.dosw.oficioya.oficioya_service.dto;

import java.util.List;
import java.util.UUID;

public record WorkerStatsResponse(
        UUID workerId,
        long completedJobs,
        double monthlyEarnings,
        List<String> topServices
) {
}