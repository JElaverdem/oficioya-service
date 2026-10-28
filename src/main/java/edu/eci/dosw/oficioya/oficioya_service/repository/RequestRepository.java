package edu.eci.dosw.oficioya.oficioya_service.repository;

import edu.eci.dosw.oficioya.oficioya_service.entity.Request;
import edu.eci.dosw.oficioya.oficioya_service.model.RequestState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RequestRepository extends JpaRepository<Request, UUID> {

    Optional<Request> findById(UUID id);

    List<Request> findByWorkerId(UUID workerId);

    List<Request> findByContractorId(UUID contractorId);

    List<Request> findByRootId(UUID rootId);

    List<Request> findByWorkerIdAndState(UUID workerId, RequestState state);

    List<Request> findByRootIdAndState(UUID rootId, RequestState state);

    List<Request> findByStateAndDateCreatedBefore(RequestState state, LocalDateTime dateTime);
}
