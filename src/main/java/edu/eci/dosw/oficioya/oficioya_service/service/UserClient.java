package edu.eci.dosw.oficioya.oficioya_service.service;

import java.util.UUID;

import edu.eci.dosw.oficioya.oficioya_service.dto.UserResponse;

public interface UserClient {
    UserResponse getUser(UUID userId);
}