package com.alexp.aws.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para respuestas de error consistentes
 * 
 * Este objeto se serializa a JSON y se devuelve al cliente cuando hay un error.
 * 
 * Ejemplo de respuesta:
 * {
 *   "timestamp": "2026-03-02T10:30:00",
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "File size exceeds 10MB limit",
 *   "path": "/api/documents"
 * }
 * 
 * @Data (Lombok): Genera getters, setters, toString, equals, hashCode
 * @Builder (Lombok): Permite construir objetos: ErrorResponse.builder().status(400).build()
 * @NoArgsConstructor: Constructor sin argumentos
 * @AllArgsConstructor: Constructor con todos los argumentos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    /**
     * Timestamp de cuando ocurrió el error
     * @JsonFormat formatea la fecha en ISO-8601
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    /**
     * Código de estado HTTP (400, 404, 500, etc.)
     */
    private int status;
    
    /**
     * Nombre del error HTTP (Bad Request, Not Found, etc.)
     */
    private String error;
    
    /**
     * Mensaje descriptivo del error
     */
    private String message;
    
    /**
     * Path/endpoint donde ocurrió el error
     */
    private String path;
}
