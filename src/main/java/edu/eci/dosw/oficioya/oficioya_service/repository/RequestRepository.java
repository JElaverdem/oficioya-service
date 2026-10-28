package edu.eci.dosw.oficioya.oficioya_service.repository;

import edu.eci.dosw.oficioya.oficioya_service.entity.Request;
import edu.eci.dosw.oficioya.oficioya_service.model.CancellationState;
import edu.eci.dosw.oficioya.oficioya_service.model.RequestState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RequestRepository extends JpaRepository<Request, UUID> {

    List<Request> findByContractorId(UUID contractorId);

    List<Request> findByWorkerId(UUID workerId);

    List<Request> findByRootId(UUID rootId);

    List<Request> findByWorkerIdAndState(UUID workerId, RequestState state);

    List<Request> findByRootIdAndState(UUID rootId, RequestState state);

    List<Request> findByStateAndDateCreatedBefore(RequestState state, LocalDateTime dateTime);

    @Query("SELECT r FROM Request r WHERE r.id = :id")
    Optional<Request> findRequestById(@Param("id") UUID id);

    @Query("""
    SELECT r FROM Request r
    WHERE r.contractorId = :contractorId
      AND r.workerId = :workerId
      AND r.cancellationState = edu.eci.dosw.oficioya.oficioya_service.model.CancellationState.IN_PROGRESS
      AND r.cancellationDateTime IS NOT NULL
      AND r.state <> edu.eci.dosw.oficioya.oficioya_service.model.RequestState.DELETED
    """)
    Optional<Request> findContractorHasCancelledWorker(@Param("contractorId") UUID contractorId, @Param("workerId") UUID workerId);

    @Query("""
    SELECT r FROM Request r
    WHERE (r.contractorId = :user OR r.workerId = :user)
      AND r.state <> edu.eci.dosw.oficioya.oficioya_service.model.RequestState.DELETED
    """)
    List<Request> findAllRequestsForUser(@Param("user") UUID user);

    @Query("""
    SELECT r FROM Request r
    WHERE (r.contractorId = :user OR r.workerId = :user)
      AND r.state <> :state
    """)
    List<Request> findAllRequestsForUserAndStateNot(@Param("user") UUID user, @Param("state") RequestState state);
}
