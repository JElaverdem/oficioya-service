package edu.eci.dosw.oficioya.oficioya_service.mapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import edu.eci.dosw.oficioya.oficioya_service.dto.RequestRequest;
import edu.eci.dosw.oficioya.oficioya_service.dto.RequestResponse;
import edu.eci.dosw.oficioya.oficioya_service.entity.Request;
import edu.eci.dosw.oficioya.oficioya_service.model.RequestState;

@Component 
public class ResponseMapper {
    public RequestResponse toRespone(List<Request> reqs){
        List<UUID> workers = reqs.stream().map(Request::getWorkerId).toList();
        Request oneRequest = reqs.get(0);
        return new RequestResponse(oneRequest.getRootId(), workers, oneRequest.getDescription(), oneRequest.getScheduledDateTime(), reqs.size());
    }


    public List<Request> toEntities(UUID contractorId, List<UUID> workers, RequestRequest req, UUID rootId){
        List<Request> createdEntities = new ArrayList<>();
        Request createdRequest = null;

        for (UUID workerId : workers){
            createdRequest = new Request();
            createdRequest.setRootId(rootId);
            createdRequest.setContractorId(contractorId);
            createdRequest.setWorkerId(workerId);
            createdRequest.setDescription(req.description());
            createdRequest.setNeighborhood(req.neighborhood());
            createdRequest.setExactAddress(req.exactAddress());
            createdRequest.setScheduledDateTime(req.scheduledDateTime());
            createdRequest.setState(RequestState.PENDING);
            createdRequest.setDateCreated(LocalDateTime.now());

            createdEntities.add(createdRequest);
        }

        return createdEntities;
    }
}
