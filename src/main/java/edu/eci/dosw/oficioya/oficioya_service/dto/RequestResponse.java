package edu.eci.dosw.oficioya.oficioya_service.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RequestResponse (
    UUID rootId,
    List<UUID> workersId, //A quienes fue enviado
    String description,
    LocalDateTime scheduledDateTime,
    int sent //A cuantos fue enviado
    ){
    
}
