package edu.eci.dosw.oficioya.oficioya_service.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        List<String> phones,
        List<String> roles,
        String accountStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}