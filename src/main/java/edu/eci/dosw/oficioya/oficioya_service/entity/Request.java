package edu.eci.dosw.oficioya.oficioya_service.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import edu.eci.dosw.oficioya.oficioya_service.model.RequestState;
import jakarta.persistence.*;

@Entity 
public class Request {
    @Id
    private UUID id;

    @Column (name = "contractor_id", nullable = false)
    private UUID contractorId;

    @Column (name = "worker_id", nullable = false)
    private UUID workerId;
    
    @Column (nullable = false)
    private String description;
    private String photoUrl;

    @Column (nullable = false)
    private String neighborhood;

    @Column (name = "exact_address", nullable = false)
    private String exactAddress;

    @Column (name = "date_scheduled", nullable = false)
    private LocalDateTime scheduledDateTime;

    @Enumerated(EnumType.STRING)
    private RequestState state;

    @Column (name = "date_created", nullable = false)
    private LocalDateTime dateTimeCreated;

    public Request(){

    }

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

    public void setDescription(String newDes){
        this.description = newDes;
    }

    public String getDescription(){
        return this.description;
    }

    public void setNeighborhood(String newNeigh){
        this.neighborhood = newNeigh;
    }

    public String getNeighborhood(){
        return this.neighborhood;
    }

    public void setExactAddress(String newAdd){
        this.exactAddress = newAdd;
    }

    public String getExactAddress(){
        return this.exactAddress;
    }

    public void setScheduledDateTime(LocalDateTime newDate){
        this.scheduledDateTime = newDate;
    }

    public LocalDateTime getScheduledDateTime(){
        return this.scheduledDateTime;
    }

    public void setState(RequestState newState){
        this.state = newState;
    }

    public RequestState getState(){
        return this.state;
    }

    public void setDateTimeCreated(LocalDateTime newDate){
        this.dateTimeCreated = newDate;
    }

    public LocalDateTime getDateTimeCreated(){
        return this.dateTimeCreated;
    }
}
