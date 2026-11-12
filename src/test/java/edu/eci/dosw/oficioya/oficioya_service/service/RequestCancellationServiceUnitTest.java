package edu.eci.dosw.oficioya.oficioya_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import edu.eci.dosw.oficioya.oficioya_service.entity.Request;
import edu.eci.dosw.oficioya.oficioya_service.mapper.RequestMapper;
import edu.eci.dosw.oficioya.oficioya_service.model.RequestModel;
import edu.eci.dosw.oficioya.oficioya_service.model.RequestState;
import edu.eci.dosw.oficioya.oficioya_service.model.CancellationState;
import edu.eci.dosw.oficioya.oficioya_service.repository.RequestRepository;
import edu.eci.dosw.oficioya.oficioya_service.service.NotificationService;

@ExtendWith(MockitoExtension.class)
class RequestCancellationServiceUnitTest {

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private RequestMapper requestMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private RequestCancellationService service;

    @Test
    void cancelByClientSuccess() {
        UUID requestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Request request = new Request();
        request.setId(requestId);
        request.setContractorId(userId);
        request.setWorkerId(UUID.randomUUID());
        request.setState(RequestState.PENDING);
        request.setScheduledDateTime(LocalDateTime.now().plusDays(2));
        request.setDescription("desc");
        request.setNeighborhood("zone");
        request.setExactAddress("addr");
        request.setDateCreated(LocalDateTime.now());

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> inv.getArgument(0));
        when(requestMapper.toModel(any(Request.class))).thenReturn(new RequestModel());

        RequestModel result = service.cancelRequest(requestId, userId);

        assertEquals(RequestState.CANCELED, request.getState());
    }

    @Test
    void cancelByWorkerSuccess() {
        UUID requestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Request request = new Request();
        request.setId(requestId);
        request.setContractorId(UUID.randomUUID());
        request.setWorkerId(userId);
        request.setState(RequestState.PENDING);
        request.setScheduledDateTime(LocalDateTime.now().plusDays(2));
        request.setDescription("desc");
        request.setNeighborhood("zone");
        request.setExactAddress("addr");
        request.setDateCreated(LocalDateTime.now());

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> inv.getArgument(0));
        when(requestMapper.toModel(any(Request.class))).thenReturn(new RequestModel());

        RequestModel result = service.cancelRequest(requestId, userId);

        assertEquals(RequestState.CANCELED, request.getState());
    }

    @Test
    void cancelByClientLateSetsInProgress() {
        UUID requestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Request request = new Request();
        request.setId(requestId);
        request.setContractorId(userId);
        request.setWorkerId(UUID.randomUUID());
        request.setState(RequestState.ACCEPTED);
        request.setScheduledDateTime(LocalDateTime.now().plusHours(1));
        request.setDescription("desc");
        request.setNeighborhood("zone");
        request.setExactAddress("addr");
        request.setDateCreated(LocalDateTime.now());

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> inv.getArgument(0));
        when(requestMapper.toModel(any(Request.class))).thenReturn(new RequestModel());

        service.cancelRequest(requestId, userId);

        assertEquals(CancellationState.IN_PROGRESS, request.getCancellationState());
    }
}