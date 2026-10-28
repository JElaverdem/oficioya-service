package edu.eci.dosw.oficioya.oficioya_service.model;

import java.util.UUID;

import java.time.LocalDateTime;

public class RequestModel {
    private UUID id;
    private UUID rootId;
    private UUID contractorId;
    private UUID workerId;
    private String description;
    private String neighborhood;
    private String exactAddress;
    private LocalDateTime scheduledDateTime;
    private RequestState state;
    private LocalDateTime dateCreated;
    private LocalDateTime cancellationDateTime;
    private CancellationState cancellationState;

    public RequestModel(){

    }

    public void setId(UUID newId){
        this.id = newId;
    }

    public UUID getId(){
        return this.id;
    }

    public void setRootId(UUID newId){
        this.rootId = newId;
    }

    public UUID getRootId(){
        return this.rootId;
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

    public void setDateCreated(LocalDateTime newDate){
        this.dateCreated = newDate;
    }

    public LocalDateTime getDateCreated(){
        return this.dateCreated;
    }
    
    public void setCancellationDateTime(LocalDateTime cancellationDateTime) {
        this.cancellationDateTime = cancellationDateTime;
    }

    public LocalDateTime getCancellationDateTime() {
    return this.cancellationDateTime;
    }

    public void setCancellationState(CancellationState cancellationState) {
        this.cancellationState = cancellationState;
    }

    public CancellationState getCancellationState() {
        return this.cancellationState;
    }
}
