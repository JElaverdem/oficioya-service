package edu.eci.dosw.oficioya.oficioya_service.repository;

import edu.eci.dosw.oficioya.oficioya_service.model.document.ImageDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface ImageRepository extends MongoRepository<ImageDocument, String> {
    List<ImageDocument> findByRequestId(UUID requestId);
    List<ImageDocument> findByReferenciaExterna(String referenciaExterna);
}
