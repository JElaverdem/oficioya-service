package edu.eci.dosw.oficioya.oficioya_service.controller;

import java.util.List;
import java.util.UUID;

import org.apache.catalina.connector.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.eci.dosw.oficioya.oficioya_service.dto.RequestRequest;
import edu.eci.dosw.oficioya.oficioya_service.dto.RequestResponse;
import edu.eci.dosw.oficioya.oficioya_service.model.RequestModel;
import edu.eci.dosw.oficioya.oficioya_service.model.document.ImageDocument;
import edu.eci.dosw.oficioya.oficioya_service.security.CurrentUser;
import edu.eci.dosw.oficioya.oficioya_service.service.ImageService;
import edu.eci.dosw.oficioya.oficioya_service.service.RequestCancellationService;
import edu.eci.dosw.oficioya.oficioya_service.service.RequestCompletionService;
import edu.eci.dosw.oficioya.oficioya_service.service.RequestService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/requests")
public class RequestController {

    private final RequestService requestService;
    private final ImageService imageService;
    private final RequestCancellationService requestCancellationService;
    private final RequestCompletionService requestCompletionService;

    public RequestController(RequestService requestService,ImageService imageService, RequestCancellationService requestCancellationService, RequestCompletionService requestCompletionService) {
        this.requestService = requestService;
        this.imageService = imageService;
        this.requestCancellationService = requestCancellationService;
        this.requestCompletionService = requestCompletionService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CONTRATANTE')")
    public RequestResponse create(@Valid @RequestBody RequestRequest req, Authentication authentication){
        RequestResponse response = requestService.create(req, CurrentUser.id(authentication));
        List<ImageDocument> images = req.images();

        images.forEach(image -> {image.setRootId(response.rootId()); });

        imageService.addImages(images);
        return response;
    }

    /*
        Conexión con los de búsqueda, donde hacemos un get para obtener los workers disponibles para ese tipo de trabajo
     */


    @PreAuthorize("hasAnyRole('CONTRATANTE', 'TRABAJADOR')")
    @GetMapping
    public List<RequestModel> listMine(Authentication authentication) {
        return requestService.listRequestsForUser(CurrentUser.id(authentication));
    }

    @PreAuthorize("hasRole('TRABAJADOR')")
    @PatchMapping("/{id}/accept")
    public RequestModel accept(@PathVariable UUID id, Authentication authentication) {
        return requestService.acceptRequest(id, CurrentUser.id(authentication));
    }

    @PreAuthorize("hasRole('TRABAJADOR')")
    @PatchMapping("/{id}/reject")
    public RequestModel reject(@PathVariable UUID id, Authentication authentication) {
        return requestService.rejectRequest(id, CurrentUser.id(authentication));
    }

    @PreAuthorize("hasRole('CONTRATANTE')")
    @PatchMapping("/{id}/accept-conflict")
    public RequestModel acceptConflict(@PathVariable UUID id, Authentication authentication) {
        return requestService.acceptConflict(id, CurrentUser.id(authentication));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('CONTRATANTE', 'TRABAJADOR')")
    public RequestModel cancelRequest(@PathVariable UUID id, Authentication authentication){
        return requestCancellationService.cancelRequest(id,CurrentUser.id(authentication));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasRole('TRABAJADOR')")
    public RequestModel completeRequest(@PathVariable UUID id, Authentication authentication){
        return requestCompletionService.completeRequest(id, CurrentUser.id(authentication));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CONTRATANTE')")
    public ResponseEntity<String> deleteRequest(@PathVariable UUID id){
        if(requestService.deleteRequest(id)){
            return ResponseEntity.status(HttpStatus.OK).body("Request deleted");
        }
        else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Request not deleted");
        }
    }
}
