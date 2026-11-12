package edu.eci.dosw.oficioya.oficioya_service.dto;

import java.util.Map;

public record ServiceReportResponse(
        long total,
        Map<String, Long> byStatus,
        Map<String, Long> byNeighborhood,
        Map<Integer, Long> byHour
) {
}