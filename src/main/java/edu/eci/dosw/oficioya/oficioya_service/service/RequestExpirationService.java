package edu.eci.dosw.oficioya.oficioya_service.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import edu.eci.dosw.oficioya.oficioya_service.entity.Request;
import edu.eci.dosw.oficioya.oficioya_service.model.RequestState;
import edu.eci.dosw.oficioya.oficioya_service.repository.RequestRepository;

@Service
public class RequestExpirationService {

    private final RequestRepository requestRepository;

    public RequestExpirationService(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    public void expirePendingRequests() {
        LocalDateTime limit = LocalDateTime.now().minusMinutes(30);
        List<Request> expired = requestRepository.findByStateAndDateCreatedBefore(RequestState.PENDING, limit);
        for (Request request : expired) {
            request.setState(RequestState.EXPIRED);
            requestRepository.save(request);
        }
    }
}
