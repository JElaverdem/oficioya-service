package edu.eci.dosw.oficioya.oficioya_service.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import edu.eci.dosw.oficioya.oficioya_service.entity.Request;
import edu.eci.dosw.oficioya.oficioya_service.model.CancellationState;
import edu.eci.dosw.oficioya.oficioya_service.model.RequestModel;
import edu.eci.dosw.oficioya.oficioya_service.model.RequestState;
import edu.eci.dosw.oficioya.oficioya_service.mapper.RequestMapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;

@DataJpaTest
@ActiveProfiles("test")
public class RequestRepositoryTest {
    @Autowired 
    private RequestRepository requestRepo;

    private final RequestMapper rMap = Mappers.getMapper(RequestMapper.class);

    private Request r1;

    @BeforeEach 
    void setup(){
        requestRepo.deleteAll();
        r1 = new Request();
        UUID r1Id = UUID.randomUUID();
        UUID r1Root = UUID.randomUUID();
        UUID r1Contractor = UUID.randomUUID();
        UUID r1Worker = UUID.randomUUID();
        String description = "Reparar una gotera";
        String neighborhood = "Niza Antigua";
        String exactAddress = "Cra. 70a # 118-33";
        LocalDateTime scheduledDateTime = LocalDateTime.now();
        RequestState state = RequestState.PENDING;
        LocalDateTime dateCreated = LocalDateTime.now();
        LocalDateTime cancellationDateTime = null;
        CancellationState cancellationState = null;
        
        r1.setId(r1Id);
        r1.setRootId(r1Root);
        r1.setContractorId(r1Contractor);
        r1.setWorkerId(r1Worker);
        r1.setDescription(description);
        r1.setNeighborhood(neighborhood);
        r1.setExactAddress(exactAddress);
        r1.setScheduledDateTime(scheduledDateTime);
        r1.setState(state);
        r1.setDateCreated(dateCreated);
        r1.setCancellationDateTime(cancellationDateTime);
        r1.setCancellationState(cancellationState);
    }

    @Test 
    @DisplayName("Debe salvar una solicitud básica")
    void shouldSaveBasicRequest(){
        Request saved = requestRepo.save(r1);

        Optional<Request> found = requestRepo.findById(saved.getId());

        assertFalse(found.isEmpty());
        assertEquals(found.orElse(null),saved);
        Request using = found.orElse(null);
        assertEquals(using.getId(),saved.getId());
        assertEquals(using.getContractorId(),saved.getContractorId());
        assertEquals(using.getRootId(),saved.getRootId());
        assertEquals(using.getWorkerId(),saved.getWorkerId());
    }

    @Test 
    @DisplayName("Debe salvar y consultar solicitudes provenientes de la 'misma pasarela'")
    void shouldSaveAndFindRequestsFromSameRoot(){
        Request r2 = new Request();
        UUID r2Id = UUID.randomUUID();
        UUID r2Root = r1.getRootId();
        UUID r2Contractor = r1.getContractorId();
        UUID r2Worker = UUID.randomUUID();
        String description = "Reparar una gotera";
        String neighborhood = "Niza Antigua";
        String exactAddress = "Cra. 70a # 118-33";
        LocalDateTime scheduledDateTime = LocalDateTime.now();
        RequestState state = RequestState.PENDING;
        LocalDateTime dateCreated = LocalDateTime.now();
        LocalDateTime cancellationDateTime = null;
        CancellationState cancellationState = null;
        
        r2.setId(r2Id);
        r2.setRootId(r2Root);
        r2.setContractorId(r2Contractor);
        r2.setWorkerId(r2Worker);
        r2.setDescription(description);
        r2.setNeighborhood(neighborhood);
        r2.setExactAddress(exactAddress);
        r2.setScheduledDateTime(scheduledDateTime);
        r2.setState(state);
        r2.setDateCreated(dateCreated);
        r2.setCancellationDateTime(cancellationDateTime);
        r2.setCancellationState(cancellationState);

        Request r1Saved = requestRepo.save(r1);
        Request r2Saved = requestRepo.save(r2);

        List<Request> ls = requestRepo.findByRootId(r1Saved.getRootId());

        assertTrue(ls.contains(r1Saved));
        assertTrue(ls.contains(r2Saved));
        Request list1 = ls.get(0);
        Request list2 = ls.get(1);

        assertEquals(list1.getId(),r1Saved.getId());
        assertEquals(list2.getId(),r2Saved.getId());

        assertEquals(list1.getRootId(),r1Saved.getRootId());
        assertEquals(list2.getRootId(),r2Saved.getRootId());

        assertEquals(list2.getRootId(),r1Saved.getRootId());
        assertEquals(list1.getRootId(),r2Saved.getRootId());

        assertEquals(r1Saved.getRootId(),r2Saved.getRootId());
        assertEquals(list1.getRootId(),list2.getRootId());

        assertEquals(list1.getContractorId(),r1Saved.getContractorId());
        assertEquals(list2.getContractorId(),r2Saved.getContractorId());
    }

    @Test 
    @DisplayName("El usuario no debe ver solicitudes eliminadas")
    void shouldNotSeeUserCanceledRequests(){
        Request r2 = new Request();
        UUID r2Id = UUID.randomUUID();
        UUID r2Root = r1.getRootId();
        UUID r2Contractor = r1.getContractorId();
        UUID r2Worker = UUID.randomUUID();
        String description = "Reparar una gotera";
        String neighborhood = "Niza Antigua";
        String exactAddress = "Cra. 70a # 118-33";
        LocalDateTime scheduledDateTime = LocalDateTime.now();
        RequestState state = RequestState.DELETED;
        LocalDateTime dateCreated = LocalDateTime.now();
        LocalDateTime cancellationDateTime = null;
        CancellationState cancellationState = null;
        
        r2.setId(r2Id);
        r2.setRootId(r2Root);
        r2.setContractorId(r2Contractor);
        r2.setWorkerId(r2Worker);
        r2.setDescription(description);
        r2.setNeighborhood(neighborhood);
        r2.setExactAddress(exactAddress);
        r2.setScheduledDateTime(scheduledDateTime);
        r2.setState(state);
        r2.setDateCreated(dateCreated);
        r2.setCancellationDateTime(cancellationDateTime);
        r2.setCancellationState(cancellationState);

        Request saved1 = requestRepo.save(r1);
        Request saved2 = requestRepo.save(r2);

        //Lista lógica que se utilizaría internamente con todas las solicitudes
        List<Request> logicList = requestRepo.findByContractorIdOrWorkerId(saved1.getContractorId(), saved1.getContractorId());

        //Lista que sólo se le mostraría al usuario, excluye las eliminadas
        List<Request> userList = requestRepo.findAllRequestsForUser(saved1.getContractorId());

        assertTrue(logicList.contains(saved1));
        assertTrue(logicList.contains(saved2));

        assertTrue(userList.contains(saved1));
        assertFalse(userList.contains(saved2));

        assertEquals(logicList.size(),2);
        assertEquals(userList.size(),1);
    }

    @Test
    @DisplayName("Debe realizar eliminacion lógica y no física")
    void shouldDeleteLogicallyNotPhysically(){
        Request r2 = new Request();
        UUID r2Id = UUID.randomUUID();
        UUID r2Root = r1.getRootId();
        UUID r2Contractor = r1.getContractorId();
        UUID r2Worker = UUID.randomUUID();
        String description = "Reparar una gotera";
        String neighborhood = "Niza Antigua";
        String exactAddress = "Cra. 70a # 118-33";
        LocalDateTime scheduledDateTime = LocalDateTime.now();
        RequestState state = RequestState.ACCEPTED;
        LocalDateTime dateCreated = LocalDateTime.now();
        LocalDateTime cancellationDateTime = null;
        CancellationState cancellationState = null;
        
        r2.setId(r2Id);
        r2.setRootId(r2Root);
        r2.setContractorId(r2Contractor);
        r2.setWorkerId(r2Worker);
        r2.setDescription(description);
        r2.setNeighborhood(neighborhood);
        r2.setExactAddress(exactAddress);
        r2.setScheduledDateTime(scheduledDateTime);
        r2.setState(state);
        r2.setDateCreated(dateCreated);
        r2.setCancellationDateTime(cancellationDateTime);
        r2.setCancellationState(cancellationState);

        Request saved1 = requestRepo.save(r1);
        Request saved2 = requestRepo.save(r2);

        List<Request> ls = requestRepo.findAllRequestsForUser(saved1.getContractorId());

        //El usuario debe ver las dos
        assertTrue(ls.contains(saved1));
        assertTrue(ls.contains(saved2));

        assertEquals(ls.size(),2);

        int numDeleted = requestRepo.specialDeleteById(saved2.getId());

        assertEquals(1,numDeleted);

        List<Request> changedList = requestRepo.findAllRequestsForUser(saved1.getContractorId());

        //El usuario ya no debe de ver las dos
        assertTrue(changedList.contains(saved1));
        assertFalse(changedList.contains(saved2));

        assertEquals(changedList.size(),1);
    }

    @Test 
    @DisplayName("Debe hacer modificaciones de estado")
    void shouldModifyState(){
        Request saved = requestRepo.save(r1);
        RequestState requestState = saved.getState();

        Optional<Request> foundOptional = requestRepo.findById(saved.getId());
        assertFalse(foundOptional.isEmpty());

        Request found = foundOptional.orElse(null);
        assertNotNull(found);

        found.setState(RequestState.REJECTED);

        Request saved2 = requestRepo.save(found);

        foundOptional = requestRepo.findById(saved2.getId());
        assertFalse(foundOptional.isEmpty());
        found = foundOptional.orElse(null);

        assertEquals(saved2.getId(),found.getId());
        assertEquals(found.getId(), saved.getId());
        assertNotEquals(found.getState(), requestState);
        assertEquals(found.getDateCreated(),saved.getDateCreated());
        assertEquals(found.getExactAddress(),saved.getExactAddress());
        assertEquals(found.getScheduledDateTime(),saved.getScheduledDateTime());
    }

    @Test 
    @DisplayName("Debe filtrar estado en query personalizada")
    void shouldFilterByStateNot(){
        Request r2 = new Request();
        UUID r2Id = UUID.randomUUID();
        UUID r2Root = r1.getRootId();
        UUID r2Contractor = r1.getContractorId();
        UUID r2Worker = UUID.randomUUID();
        String description = "Reparar una gotera";
        String neighborhood = "Niza Antigua";
        String exactAddress = "Cra. 70a # 118-33";
        LocalDateTime scheduledDateTime = LocalDateTime.now();
        RequestState state = RequestState.REJECTED;
        LocalDateTime dateCreated = LocalDateTime.now();
        LocalDateTime cancellationDateTime = null;
        CancellationState cancellationState = null;
        
        r2.setId(r2Id);
        r2.setRootId(r2Root);
        r2.setContractorId(r2Contractor);
        r2.setWorkerId(r2Worker);
        r2.setDescription(description);
        r2.setNeighborhood(neighborhood);
        r2.setExactAddress(exactAddress);
        r2.setScheduledDateTime(scheduledDateTime);
        r2.setState(state);
        r2.setDateCreated(dateCreated);
        r2.setCancellationDateTime(cancellationDateTime);
        r2.setCancellationState(cancellationState);

        Request r1Saved = requestRepo.save(r1);
        Request r2Saved = requestRepo.save(r2);

        List<Request> completeList = requestRepo.findAllRequestsForUser(r1.getContractorId());
        List<Request> filteredList = requestRepo.findAllRequestsForUserAndStateNot(r1.getContractorId(), RequestState.REJECTED);

        assertEquals(completeList.size(),2);
        assertEquals(filteredList.size(),1);

        assertTrue(completeList.contains(r1Saved));
        assertTrue(completeList.contains(r2Saved));

        assertTrue(filteredList.contains(r1Saved));
        assertFalse(filteredList.contains(r2Saved));
    }

    @Test 
    @DisplayName("Debe mostrar todas las solicitudes de un usuario, incluso si es trabajador y contratante")
    void shouldShowAllRequestsEvenIfUserIsWorkerAndContractor(){
        Request r2 = new Request();
        UUID r2Id = UUID.randomUUID();
        UUID r2Root = UUID.randomUUID();
        UUID r2Contractor = UUID.randomUUID();
        UUID r2Worker = r1.getContractorId();
        String description = "Reparar una gotera";
        String neighborhood = "Niza Antigua";
        String exactAddress = "Cra. 70a # 118-33";
        LocalDateTime scheduledDateTime = LocalDateTime.now();
        RequestState state = RequestState.REJECTED;
        LocalDateTime dateCreated = LocalDateTime.now();
        LocalDateTime cancellationDateTime = null;
        CancellationState cancellationState = null;
        
        r2.setId(r2Id);
        r2.setRootId(r2Root);
        r2.setContractorId(r2Contractor);
        r2.setWorkerId(r2Worker);
        r2.setDescription(description);
        r2.setNeighborhood(neighborhood);
        r2.setExactAddress(exactAddress);
        r2.setScheduledDateTime(scheduledDateTime);
        r2.setState(state);
        r2.setDateCreated(dateCreated);
        r2.setCancellationDateTime(cancellationDateTime);
        r2.setCancellationState(cancellationState);

        Request r1Saved = requestRepo.save(r1);
        Request r2Saved = requestRepo.save(r2);

        List<Request> list = requestRepo.findAllRequestsForUser(r1Saved.getContractorId());
        assertEquals(list.size(),2);

        assertTrue(list.contains(r1Saved));
        assertTrue(list.contains(r2Saved));

        assertEquals(list.get(0).getContractorId(),list.get(1).getWorkerId());
        assertNotEquals(list.get(0).getId(),list.get(1).getId());
    }

    @Test 
    @DisplayName("Debe encontrar la solicitud que genera bloqueo entre el trabajador y contratante")
    void shouldFindCancelledWithPenalizationRequest(){
        r1.setState(RequestState.CANCELED);
        r1.setCancellationDateTime(r1.getDateCreated().plusDays(1));
        r1.setCancellationState(CancellationState.IN_PROGRESS);

        Request saved = requestRepo.save(r1);
        
        Optional<Request> optionalFound = requestRepo.findContractorHasCancelledWorker(saved.getContractorId(), saved.getWorkerId());
        assertFalse(optionalFound.isEmpty());

        Request found = optionalFound.orElse(null);
        assertNotNull(found);

        assertEquals(saved.getId(),found.getId());
        assertEquals(found.getCancellationState(),CancellationState.IN_PROGRESS);
        assertNotNull(found.getCancellationDateTime());
        assertNotNull(found.getCancellationState());
        assertTrue(found.getCancellationDateTime().isAfter(found.getDateCreated()));
    }

    @Test 
    @DisplayName("No debe encontrar la solicitud que generó bloqueo al ser completada")
    void shouldNotFindCancelledWithPenalizationCompletedRequest(){
        r1.setState(RequestState.CANCELED);
        r1.setCancellationDateTime(r1.getDateCreated().plusDays(1));
        r1.setCancellationState(CancellationState.IN_PROGRESS);

        Request saved = requestRepo.save(r1);
        
        Optional<Request> optionalFound = requestRepo.findContractorHasCancelledWorker(saved.getContractorId(), saved.getWorkerId());
        assertFalse(optionalFound.isEmpty());

        Request found = optionalFound.orElse(null);
        assertNotNull(found);

        assertEquals(saved.getId(),found.getId());
        assertEquals(found.getCancellationState(),CancellationState.IN_PROGRESS);
        assertNotNull(found.getCancellationDateTime());
        assertNotNull(found.getCancellationState());
        assertTrue(found.getCancellationDateTime().isAfter(found.getDateCreated()));

        found.setCancellationState(CancellationState.COMPLETED);

        requestRepo.save(found);

        Optional<Request> optionalNotFound = requestRepo.findContractorHasCancelledWorker(saved.getContractorId(), saved.getWorkerId());
        assertTrue(optionalNotFound.isEmpty());

        Request notFound = optionalNotFound.orElse(null);
        assertNull(notFound);

    }
}