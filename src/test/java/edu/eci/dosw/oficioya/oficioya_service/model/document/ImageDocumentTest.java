package edu.eci.dosw.oficioya.oficioya_service.model.document;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** SNOY-20: el documento de imagen guarda y devuelve sus datos. */
class ImageDocumentTest {

    @Test
    @DisplayName("el constructor con parámetros asigna los campos")
    void constructorCompleto() {
        byte[] datos = { 1, 2, 3 };
        LocalDateTime fecha = LocalDateTime.now();

        ImageDocument doc = new ImageDocument("gotera.png", "image/png", 3L, datos, fecha, "ref-externa");

        assertEquals("gotera.png", doc.getNombre());
        assertEquals("image/png", doc.getTipoContenido());
        assertEquals(3L, doc.getTamano());
        assertArrayEquals(datos, doc.getDatos());
        assertEquals(fecha, doc.getFechaCarga());
        assertEquals("ref-externa", doc.getReferenciaExterna());
        assertNull(doc.getId());
        assertNull(doc.getRequestId());
    }

    @Test
    @DisplayName("los setters reemplazan los valores del constructor vacío")
    void settersYGetters() {
        ImageDocument doc = new ImageDocument();
        UUID id = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        byte[] datos = { 9, 8 };
        LocalDateTime fecha = LocalDateTime.now();

        doc.setId(id);
        doc.setRequestId(requestId);
        doc.setNombre("foto.jpg");
        doc.setTipoContenido("image/jpeg");
        doc.setTamano(2L);
        doc.setDatos(datos);
        doc.setFechaCarga(fecha);
        doc.setReferenciaExterna("otra-ref");

        assertEquals(id, doc.getId());
        assertEquals(requestId, doc.getRequestId());
        assertEquals("foto.jpg", doc.getNombre());
        assertEquals("image/jpeg", doc.getTipoContenido());
        assertEquals(2L, doc.getTamano());
        assertArrayEquals(datos, doc.getDatos());
        assertEquals(fecha, doc.getFechaCarga());
        assertEquals("otra-ref", doc.getReferenciaExterna());
    }
}
