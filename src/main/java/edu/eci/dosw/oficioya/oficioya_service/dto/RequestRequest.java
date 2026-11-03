package edu.eci.dosw.oficioya.oficioya_service.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import edu.eci.dosw.oficioya.oficioya_service.model.document.ImageDocument;

public record RequestRequest (
    List<UUID> workersId, //A quienes se los quiero enviar
    String description,
    String neighborhood,
    String exactAddress,
    LocalDateTime scheduledDateTime,
    List<ImageDocument> images
    ){
    
}
