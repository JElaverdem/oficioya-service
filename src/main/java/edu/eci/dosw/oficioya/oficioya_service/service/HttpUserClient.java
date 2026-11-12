package edu.eci.dosw.oficioya.oficioya_service.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import edu.eci.dosw.oficioya.oficioya_service.dto.UserResponse;

@Service
public class HttpUserClient implements UserClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String serviceToken;

    public HttpUserClient(
            @Value("${user.service.url:http://localhost:8081}") String baseUrl,
            @Value("${service.token:}") String serviceToken) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.serviceToken = serviceToken;
    }

    @Override
    public UserResponse getUser(UUID userId) {
        if (serviceToken == null || serviceToken.isBlank()) {
            return null; // sin token de servicio, se omite la verificación
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(serviceToken);
            headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
            ResponseEntity<UserResponse> response = restTemplate.exchange(
                    baseUrl + "/api/v1/admin/users/" + userId,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    UserResponse.class);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }
}