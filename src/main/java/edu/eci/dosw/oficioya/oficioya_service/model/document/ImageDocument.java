package edu.eci.dosw.oficioya.oficioya_service.model.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "imagenes")
public class ImageDocument {

    @Id
    private UUID id;

    private UUID requestId;

    private String nombre;
    private String tipoContenido;
    private Long tamano;
    private byte[] datos;
    private LocalDateTime fechaCarga;
    private String referenciaExterna;

    public ImageDocument() {
    }

    public ImageDocument(String nombre, String tipoContenido, Long tamano,
                          byte[] datos, LocalDateTime fechaCarga, String referenciaExterna) {
        this.nombre = nombre;
        this.tipoContenido = tipoContenido;
        this.tamano = tamano;
        this.datos = datos;
        this.fechaCarga = fechaCarga;
        this.referenciaExterna = referenciaExterna;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID newId){
        this.id = newId;
    }

    public void setRequestId(UUID requestId){
        this.requestId = requestId;
    }

    public UUID getRequestId(){
        return this.requestId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipoContenido() {
        return tipoContenido;
    }

    public void setTipoContenido(String tipoContenido) {
        this.tipoContenido = tipoContenido;
    }

    public Long getTamano() {
        return tamano;
    }

    public void setTamano(Long tamano) {
        this.tamano = tamano;
    }

    public byte[] getDatos() {
        return datos;
    }

    public void setDatos(byte[] datos) {
        this.datos = datos;
    }

    public LocalDateTime getFechaCarga() {
        return fechaCarga;
    }

    public void setFechaCarga(LocalDateTime fechaCarga) {
        this.fechaCarga = fechaCarga;
    }

    public String getReferenciaExterna() {
        return referenciaExterna;
    }

    public void setReferenciaExterna(String referenciaExterna) {
        this.referenciaExterna = referenciaExterna;
    }
}