package edu.eci.dosw.oficioya.oficioya_service.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.eci.dosw.oficioya.oficioya_service.controller.ImageController;

class ImagePlaceholderTest {

    @Test
    @DisplayName("ImageService existe pero aun no tiene comportamiento")
    void imageServiceEsInstanciable() {
        assertNotNull(new ImageService());
    }

    @Test
    @DisplayName("ImageController existe pero aun no tiene endpoints")
    void imageControllerEsInstanciable() {
        assertNotNull(new ImageController());
    }
}
