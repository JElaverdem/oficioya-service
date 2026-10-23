package edu.eci.dosw.oficioya.oficioya_service.mapper;

import edu.eci.dosw.oficioya.oficioya_service.model.RequestModel;
import edu.eci.dosw.oficioya.oficioya_service.entity.Request;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RequestMapper {
    Request toEntity(RequestModel requestModel);
    RequestModel toModel(Request request);
}
