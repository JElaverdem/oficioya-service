package edu.eci.dosw.oficioya.oficioya_service.entity;

import  edu.eci.dosw.oficioya.oficioya_service.model.CancellationState;
import java.util.UUID;

import jakarta.persistence.*;

@Entity 
@Table(name = "Cancellations")
public class Cancellation {
    @Id 
    private UUID id;

    @Column (name = "contractor_id", nullable = false)
    private UUID contractorId;

    @Column (name = "worker_id", nullable = false)
    private UUID workerId;

    @Enumerated(EnumType.STRING)
    private CancellationState state;

    public void setId(UUID newId){
        this.id = newId;
    }

    public UUID getId(){
        return this.id;
    }

    public void setContractorId(UUID newId){
        this.contractorId = newId;
    }

    public UUID getContractorId(){
        return this.contractorId;
    }

    public void setWorkerId(UUID newId){
        this.workerId = newId;
    }

    public UUID getWorkerId(){
        return this.workerId;
    }

    public void setState(CancellationState newState){
        this.state = newState;
    }

    public CancellationState getState(){
        return this.state;
    }

}
