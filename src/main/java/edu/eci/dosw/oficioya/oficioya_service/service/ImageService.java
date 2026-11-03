package edu.eci.dosw.oficioya.oficioya_service.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import edu.eci.dosw.oficioya.oficioya_service.entity.Request;
import edu.eci.dosw.oficioya.oficioya_service.model.document.ImageDocument;
import edu.eci.dosw.oficioya.oficioya_service.repository.ImageRepository;

@Service 
public class ImageService {
    private ImageRepository imageRepository;
    private RequestService requestService;

    public ImageService(ImageRepository imageRepository, RequestService requestService){
        this.imageRepository = imageRepository;
        this.requestService = requestService;
    }

    public void addImages(List<ImageDocument> images){
        imageRepository.saveAll(images);
    }

    public List<ImageDocument> findRequestImages(UUID requestId,UUID userId){
        Optional<Request> found = requestService.getRequest(requestId);

        if(found.isEmpty() || (found.orElse(null).getContractorId() != userId && found.orElse(null).getWorkerId() != userId)){
            return new ArrayList<>();
        }

        return imageRepository.findByRootId(found.orElse(null).getRootId());
  
    }
}
