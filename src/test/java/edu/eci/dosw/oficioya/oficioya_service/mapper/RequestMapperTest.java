package edu.eci.dosw.oficioya.oficioya_service.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import edu.eci.dosw.oficioya.oficioya_service.entity.Request;
import edu.eci.dosw.oficioya.oficioya_service.model.CancellationState;
import edu.eci.dosw.oficioya.oficioya_service.model.RequestModel;
import edu.eci.dosw.oficioya.oficioya_service.model.RequestState;

/** SNOY-20: el mapper copia todos los campos entre la entidad y el modelo. */
class RequestMapperTest {

    private final RequestMapper mapper = Mappers.getMapper(RequestMapper.class);

    @Test
    @DisplayName("toModel copia todos los campos de la entidad")
    void entidadAModelo() {
        Request r = entidadCompleta();

        RequestModel m = mapper.toModel(r);

        assertEquals(r.getId(), m.getId());
        assertEquals(r.getRootId(), m.getRootId());
        assertEquals(r.getContractorId(), m.getContractorId());
        assertEquals(r.getWorkerId(), m.getWorkerId());
        assertEquals(r.getDescription(), m.getDescription());
        assertEquals(r.getNeighborhood(), m.getNeighborhood());
        assertEquals(r.getExactAddress(), m.getExactAddress());
        assertEquals(r.getScheduledDateTime(), m.getScheduledDateTime());
        assertEquals(r.getState(), m.getState());
        assertEquals(r.getDateCreated(), m.getDateCreated());
        assertEquals(r.getCancellationDateTime(), m.getCancellationDateTime());
        assertEquals(r.getCancellationState(), m.getCancellationState());
    }

    @Test
    @DisplayName("toEntity copia todos los campos del modelo (ida y vuelta)")
    void modeloAEntidad() {
        Request original = entidadCompleta();
        RequestModel m = mapper.toModel(original);

        Request copia = mapper.toEntity(m);

        assertEquals(original.getId(), copia.getId());
        assertEquals(original.getRootId(), copia.getRootId());
        assertEquals(original.getContractorId(), copia.getContractorId());
        assertEquals(original.getWorkerId(), copia.getWorkerId());
        assertEquals(original.getDescription(), copia.getDescription());
        assertEquals(original.getNeighborhood(), copia.getNeighborhood());
        assertEquals(original.getExactAddress(), copia.getExactAddress());
        assertEquals(original.getScheduledDateTime(), copia.getScheduledDateTime());
        assertEquals(original.getState(), copia.getState());
        assertEquals(original.getDateCreated(), copia.getDateCreated());
        assertEquals(original.getCancellationDateTime(), copia.getCancellationDateTime());
        assertEquals(original.getCancellationState(), copia.getCancellationState());
    }

    @Test
    @DisplayName("los campos de cancelación nulos se mantienen nulos")
    void camposDeCancelacionNulos() {
        Request r = entidadCompleta();
        r.setCancellationDateTime(null);
        r.setCancellationState(null);

        RequestModel m = mapper.toModel(r);

        assertNull(m.getCancellationDateTime());
        assertNull(m.getCancellationState());
    }

    @Test
    @DisplayName("mapear null devuelve null")
    void mapearNull() {
        assertNull(mapper.toModel(null));
        assertNull(mapper.toEntity(null));
    }

    private Request entidadCompleta() {
        Request r = new Request();
        r.setId(UUID.randomUUID());
        r.setRootId(UUID.randomUUID());
        r.setContractorId(UUID.randomUUID());
        r.setWorkerId(UUID.randomUUID());
        r.setDescription("Reparar una gotera");
        r.setNeighborhood("Niza Antigua");
        r.setExactAddress("Cra. 70a # 118-33");
        r.setScheduledDateTime(LocalDateTime.now().plusDays(2));
        r.setState(RequestState.CANCELED);
        r.setDateCreated(LocalDateTime.now().minusHours(1));
        r.setCancellationDateTime(LocalDateTime.now());
        r.setCancellationState(CancellationState.COMPLETED);
        return r;
    }
}
