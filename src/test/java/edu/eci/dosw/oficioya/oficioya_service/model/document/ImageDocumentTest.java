package edu.eci.dosw.oficioya.oficioya_service.model.document;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ImageDocumentTest {

    @Test
    @DisplayName("El constructor asigna los campos de la imagen")
    void constructorAsignaCampos() {
        LocalDateTime fecha = LocalDateTime.now();
        byte[] datos = {1, 2, 3};

        ImageDocument document = new ImageDocument("foto.jpg", "image/jpeg", 3L, datos, fecha, "ref-1");

        assertEquals("foto.jpg", document.getNombre());
        assertEquals("image/jpeg", document.getTipoContenido());
        assertEquals(3L, document.getTamano());
        assertArrayEquals(datos, document.getDatos());
        assertEquals(fecha, document.getFechaCarga());
        assertEquals("ref-1", document.getReferenciaExterna());
        assertNull(document.getId());
        assertNull(document.getRequestId());
    }

    @Test
    @DisplayName("Los setters actualizan todos los campos")
    void settersActualizanCampos() {
        UUID id = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        LocalDateTime fecha = LocalDateTime.now();
        byte[] datos = {4, 5, 6};

        ImageDocument document = new ImageDocument();
        document.setId(id);
        document.setRequestId(requestId);
        document.setNombre("plano.png");
        document.setTipoContenido("image/png");
        document.setTamano(3L);
        document.setDatos(datos);
        document.setFechaCarga(fecha);
        document.setReferenciaExterna("ref-2");

        assertEquals(id, document.getId());
        assertEquals(requestId, document.getRequestId());
        assertEquals("plano.png", document.getNombre());
        assertEquals("image/png", document.getTipoContenido());
        assertEquals(3L, document.getTamano());
        assertArrayEquals(datos, document.getDatos());
        assertEquals(fecha, document.getFechaCarga());
        assertEquals("ref-2", document.getReferenciaExterna());
    }
}
