package com.alexp.aws.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Modelo que representa un documento almacenado en S3
 * 
 * Anotaciones de Lombok:
 * - @Data: Genera automáticamente getters, setters, toString(), equals(), hashCode()
 * - @Builder: Permite crear objetos con un patrón builder: Document.builder().id("123").build()
 * - @NoArgsConstructor: Genera constructor sin argumentos
 * - @AllArgsConstructor: Genera constructor con todos los argumentos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Document {
    
    /**
     * ID único del documento (UUID generado)
     */
    private String id;
    
    /**
     * Nombre descriptivo del documento (ej: "Factura Enero 2024")
     */
    private String name;
    
    /**
     * Nombre original del archivo (ej: "factura.pdf")
     */
    private String fileName;
    
    /**
     * Tipo MIME del archivo (ej: "application/pdf", "image/jpeg")
     */
    private String contentType;
    
    /**
     * Tamaño del archivo en bytes
     */
    private Long size;
    
    /**
     * Key/ruta del archivo en S3 (ej: "documents/abc-123-def.pdf")
     */
    private String s3Key;
    
    /**
     * Fecha y hora de subida
     */
    private LocalDateTime uploadDate;
}
