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

class RequestMapperTest {

    private final RequestMapper mapper = Mappers.getMapper(RequestMapper.class);

    @Test
    @DisplayName("toEntity copia todos los campos del modelo")
    void toEntityCopiaTodosLosCampos() {
        RequestModel model = modelCompleto();

        Request entity = mapper.toEntity(model);

        assertEquals(model.getId(), entity.getId());
        assertEquals(model.getRootId(), entity.getRootId());
        assertEquals(model.getContractorId(), entity.getContractorId());
        assertEquals(model.getWorkerId(), entity.getWorkerId());
        assertEquals(model.getDescription(), entity.getDescription());
        assertEquals(model.getNeighborhood(), entity.getNeighborhood());
        assertEquals(model.getExactAddress(), entity.getExactAddress());
        assertEquals(model.getScheduledDateTime(), entity.getScheduledDateTime());
        assertEquals(model.getState(), entity.getState());
        assertEquals(model.getDateCreated(), entity.getDateCreated());
        assertEquals(model.getCancellationDateTime(), entity.getCancellationDateTime());
        assertEquals(model.getCancellationState(), entity.getCancellationState());
    }

    @Test
    @DisplayName("toModel copia todos los campos de la entidad")
    void toModelCopiaTodosLosCampos() {
        Request entity = entityCompleta();

        RequestModel model = mapper.toModel(entity);

        assertEquals(entity.getId(), model.getId());
        assertEquals(entity.getRootId(), model.getRootId());
        assertEquals(entity.getContractorId(), model.getContractorId());
        assertEquals(entity.getWorkerId(), model.getWorkerId());
        assertEquals(entity.getDescription(), model.getDescription());
        assertEquals(entity.getNeighborhood(), model.getNeighborhood());
        assertEquals(entity.getExactAddress(), model.getExactAddress());
        assertEquals(entity.getScheduledDateTime(), model.getScheduledDateTime());
        assertEquals(entity.getState(), model.getState());
        assertEquals(entity.getDateCreated(), model.getDateCreated());
        assertEquals(entity.getCancellationDateTime(), model.getCancellationDateTime());
        assertEquals(entity.getCancellationState(), model.getCancellationState());
    }

    @Test
    @DisplayName("El round-trip modelo -> entidad -> modelo conserva los datos")
    void roundTripConservaDatos() {
        RequestModel original = modelCompleto();

        RequestModel result = mapper.toModel(mapper.toEntity(original));

        assertEquals(original.getId(), result.getId());
        assertEquals(original.getRootId(), result.getRootId());
        assertEquals(original.getContractorId(), result.getContractorId());
        assertEquals(original.getWorkerId(), result.getWorkerId());
        assertEquals(original.getState(), result.getState());
        assertEquals(original.getCancellationState(), result.getCancellationState());
        assertEquals(original.getScheduledDateTime(), result.getScheduledDateTime());
    }

    @Test
    @DisplayName("Los campos opcionales nulos se mantienen nulos")
    void camposOpcionalesNulosSeMantienenNulos() {        RequestModel model = new RequestModel();
        model.setId(UUID.randomUUID());
        model.setRootId(UUID.randomUUID());
        model.setContractorId(UUID.randomUUID());
        model.setWorkerId(UUID.randomUUID());
        model.setDescription("descripcion");
        model.setNeighborhood("barrio");
        model.setExactAddress("direccion");
        model.setScheduledDateTime(LocalDateTime.now().plusDays(1));
        model.setState(RequestState.PENDING);
        model.setDateCreated(LocalDateTime.now());

        Request entity = mapper.toEntity(model);

        assertNull(entity.getCancellationDateTime());
        assertNull(entity.getCancellationState());

        RequestModel result = mapper.toModel(entity);

        assertNull(result.getCancellationDateTime());
        assertNull(result.getCancellationState());
    }

    @Test
    @DisplayName("Un modelo nulo se mapea a una entidad nula")
    void modeloNuloSeMapeaANulo() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    @DisplayName("Una entidad nula se mapea a un modelo nulo")
    void entidadNulaSeMapeaANulo() {
        assertNull(mapper.toModel(null));
    }

    private RequestModel modelCompleto() {
        RequestModel model = new RequestModel();
        model.setId(UUID.randomUUID());
        model.setRootId(UUID.randomUUID());
        model.setContractorId(UUID.randomUUID());
        model.setWorkerId(UUID.randomUUID());
        model.setDescription("Reparar una gotera");
        model.setNeighborhood("Niza Antigua");
        model.setExactAddress("Cra. 70a # 118-33");
        model.setScheduledDateTime(LocalDateTime.now().plusDays(2));
        model.setState(RequestState.CANCELED);
        model.setDateCreated(LocalDateTime.now().minusDays(1));
        model.setCancellationDateTime(LocalDateTime.now());
        model.setCancellationState(CancellationState.IN_PROGRESS);
        return model;
    }

    private Request entityCompleta() {
        Request entity = new Request();
        entity.setId(UUID.randomUUID());
        entity.setRootId(UUID.randomUUID());
        entity.setContractorId(UUID.randomUUID());
        entity.setWorkerId(UUID.randomUUID());
        entity.setDescription("Reparar una gotera");
        entity.setNeighborhood("Niza Antigua");
        entity.setExactAddress("Cra. 70a # 118-33");
        entity.setScheduledDateTime(LocalDateTime.now().plusDays(2));
        entity.setState(RequestState.COMPLETED);
        entity.setDateCreated(LocalDateTime.now().minusDays(2));
        entity.setCancellationDateTime(LocalDateTime.now());
        entity.setCancellationState(CancellationState.COMPLETED);
        return entity;
    }
}
