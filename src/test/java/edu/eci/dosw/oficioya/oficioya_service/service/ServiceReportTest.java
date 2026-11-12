package edu.eci.dosw.oficioya.oficioya_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import edu.eci.dosw.oficioya.oficioya_service.dto.ServiceReportResponse;
import edu.eci.dosw.oficioya.oficioya_service.repository.RequestRepository;

@ExtendWith(MockitoExtension.class)
class ServiceReportTest {

    @Mock
    private RequestRepository requestRepository;

    @Test
    void emptyReport() {
        when(requestRepository.findAll()).thenReturn(List.of());

        RequestService service = new RequestService(requestRepository,
                org.mapstruct.factory.Mappers.getMapper(edu.eci.dosw.oficioya.oficioya_service.mapper.RequestMapper.class),
                new edu.eci.dosw.oficioya.oficioya_service.mapper.ResponseMapper(),
                new edu.eci.dosw.oficioya.oficioya_service.service.LoggingNotificationService(),
                null);

        ServiceReportResponse report = service.getReport(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));

        assertEquals(0, report.total());
    }
}