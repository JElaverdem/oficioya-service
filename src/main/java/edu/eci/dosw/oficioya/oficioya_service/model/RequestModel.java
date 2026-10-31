package edu.eci.dosw.oficioya.oficioya_service.model;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Solicitud de servicio vista por la API")
public class RequestModel {

    @Schema(description = "Identificador unico de la solicitud", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;

    @Schema(description = "Identificador del grupo de solicitudes enviadas a varios trabajadores")
    private UUID rootId;

    @Schema(description = "Usuario que solicita el servicio")
    private UUID contractorId;

    @Schema(description = "Trabajador asignado a la solicitud")
    private UUID workerId;

    @Schema(description = "Descripcion del trabajo solicitado", example = "Reparar una gotera")
    private String description;

    @Schema(description = "Barrio donde se presta el servicio", example = "Niza Antigua")
    private String neighborhood;

    @Schema(description = "Direccion exacta del servicio", example = "Cra. 70a # 118-33")
    private String exactAddress;

    @Schema(description = "Fecha y hora programada del servicio", example = "2026-11-05T09:00:00")
    private LocalDateTime scheduledDateTime;

    @Schema(description = "Estado actual de la solicitud", example = "PENDING")
    private RequestState state;

    @Schema(description = "Fecha y hora de creacion de la solicitud")
    private LocalDateTime dateCreated;

    @Schema(description = "Fecha y hora de cancelacion, si aplica")
    private LocalDateTime cancellationDateTime;

    @Schema(description = "Estado del proceso de cancelacion, si aplica", example = "COMPLETED")
    private CancellationState cancellationState;

    public RequestModel() {

    }

    public void setId(UUID newId) {
        this.id = newId;
    }

    public UUID getId() {
        return this.id;
    }

    public void setRootId(UUID newId) {
        this.rootId = newId;
    }

    public UUID getRootId() {
        return this.rootId;
    }

    public void setContractorId(UUID newId) {
        this.contractorId = newId;
    }

    public UUID getContractorId() {
        return this.contractorId;
    }

    public void setWorkerId(UUID newId) {
        this.workerId = newId;
    }

    public UUID getWorkerId() {
        return this.workerId;
    }

    public void setDescription(String newDes) {
        this.description = newDes;
    }

    public String getDescription() {
        return this.description;
    }

    public void setNeighborhood(String newNeigh) {
        this.neighborhood = newNeigh;
    }

    public String getNeighborhood() {
        return this.neighborhood;
    }

    public void setExactAddress(String newAdd) {
        this.exactAddress = newAdd;
    }

    public String getExactAddress() {
        return this.exactAddress;
    }

    public void setScheduledDateTime(LocalDateTime newDate) {
        this.scheduledDateTime = newDate;
    }

    public LocalDateTime getScheduledDateTime() {
        return this.scheduledDateTime;
    }

    public void setState(RequestState newState) {
        this.state = newState;
    }

    public RequestState getState() {
        return this.state;
    }

    public void setDateCreated(LocalDateTime newDate) {
        this.dateCreated = newDate;
    }

    public LocalDateTime getDateCreated() {
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
