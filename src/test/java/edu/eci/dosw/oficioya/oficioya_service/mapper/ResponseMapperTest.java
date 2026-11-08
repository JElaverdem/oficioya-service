package edu.eci.dosw.oficioya.oficioya_service.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import edu.eci.dosw.oficioya.oficioya_service.dto.RequestRequest;
import edu.eci.dosw.oficioya.oficioya_service.dto.RequestResponse;
import edu.eci.dosw.oficioya.oficioya_service.entity.Request;
import edu.eci.dosw.oficioya.oficioya_service.model.RequestState;

class ResponseMapperTest {

    @Test
    void toEntitiesCreatesOnePerWorker() {
        UUID contractor = UUID.randomUUID();
        UUID worker1 = UUID.randomUUID();
        UUID worker2 = UUID.randomUUID();
        UUID rootId = UUID.randomUUID();
        RequestRequest req = new RequestRequest(
                List.of(worker1, worker2),
                "desc",
                "zone",
                "addr",
                LocalDateTime.now().plusDays(1),
                null
        );

        ResponseMapper mapper = new ResponseMapper();
        var entities = mapper.toEntities(contractor, req.workersId(), req, rootId);

        assertEquals(2, entities.size());
        assertEquals(RequestState.PENDING, entities.get(0).getState());
        assertEquals(contractor, entities.get(0).getContractorId());
    }

    @Test
    void toResponseBuildsResponse() {
        UUID rootId = UUID.randomUUID();
        UUID worker = UUID.randomUUID();
        Request req = new Request();
        req.setRootId(rootId);
        req.setWorkerId(worker);
        req.setDescription("desc");
        req.setScheduledDateTime(LocalDateTime.now().plusDays(1));

        ResponseMapper mapper = new ResponseMapper();
        RequestResponse response = mapper.toRespone(List.of(req));

        assertNotNull(response);
        assertEquals(rootId, response.rootId());
        assertEquals(1, response.workersId().size());
    }
}