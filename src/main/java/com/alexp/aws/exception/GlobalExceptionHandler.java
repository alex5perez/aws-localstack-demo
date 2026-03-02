package com.alexp.aws.exception;

import com.alexp.aws.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;

/**
 * Manejador global de excepciones
 * 
 * @ControllerAdvice:
 * - Se aplica a TODOS los @Controller y @RestController de la aplicación
 * - Captura excepciones lanzadas en cualquier endpoint
 * - Centraliza el manejo de errores en un solo lugar
 * 
 * @ExceptionHandler:
 * - Define qué método maneja qué tipo de excepción
 * - Ejemplo: @ExceptionHandler(DocumentNotFoundException.class)
 *   → Este método solo captura DocumentNotFoundException
 * 
 * Flujo:
 * 1. Controller llama a Service
 * 2. Service lanza excepción: throw new InvalidFileException("...")
 * 3. Spring intercepta la excepción
 * 4. Busca un @ExceptionHandler que maneje ese tipo
 * 5. Ejecuta el método correspondiente
 * 6. Devuelve ErrorResponse como JSON al cliente
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Maneja DocumentNotFoundException
     * Devuelve HTTP 404 (Not Found)
     */
    @ExceptionHandler(DocumentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDocumentNotFound(
            DocumentNotFoundException ex, 
            WebRequest request) {
        
        log.error("Document not found: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())          // 404
                .error(HttpStatus.NOT_FOUND.getReasonPhrase()) // "Not Found"
                .message(ex.getMessage())
                .path(extractPath(request))
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Maneja InvalidFileException
     * Devuelve HTTP 400 (Bad Request)
     */
    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFile(
            InvalidFileException ex, 
            WebRequest request) {
        
        log.error("Invalid file: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())          // 400
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase()) // "Bad Request"
                .message(ex.getMessage())
                .path(extractPath(request))
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Maneja S3OperationException
     * Devuelve HTTP 500 (Internal Server Error)
     */
    @ExceptionHandler(S3OperationException.class)
    public ResponseEntity<ErrorResponse> handleS3Operation(
            S3OperationException ex, 
            WebRequest request) {
        
        log.error("S3 operation failed: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())          // 500
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()) // "Internal Server Error"
                .message("Error processing S3 operation: " + ex.getMessage())
                .path(extractPath(request))
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Maneja excepciones cuando el archivo excede el tamaño máximo configurado
     * Esta excepción la lanza Spring automáticamente
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxSizeException(
            MaxUploadSizeExceededException ex, 
            WebRequest request) {
        
        log.error("File size exceeds limit: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.PAYLOAD_TOO_LARGE.value())          // 413
                .error(HttpStatus.PAYLOAD_TOO_LARGE.getReasonPhrase()) // "Payload Too Large"
                .message("File size exceeds the maximum allowed (10MB)")
                .path(extractPath(request))
                .build();
        
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(errorResponse);
    }

    /**
     * Maneja cualquier otra excepción no capturada específicamente
     * Es el "catch-all" para errores inesperados
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, 
            WebRequest request) {
        
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("An unexpected error occurred. Please try again later.")
                .path(extractPath(request))
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Extrae el path del request
     * Ejemplo: "/api/documents/123" 
     */
    private String extractPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}
