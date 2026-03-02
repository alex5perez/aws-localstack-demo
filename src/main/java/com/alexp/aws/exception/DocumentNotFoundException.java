package com.alexp.aws.exception;

/**
 * Excepción lanzada cuando no se encuentra un documento
 * 
 * Extends RuntimeException:
 * - RuntimeException son excepciones "no checkeadas"
 * - No necesitas declarar "throws" en los métodos
 * - Spring las maneja automáticamente
 * 
 * Uso:
 * throw new DocumentNotFoundException("Document with ID abc not found");
 */
public class DocumentNotFoundException extends RuntimeException {
    
    public DocumentNotFoundException(String message) {
        super(message);
    }
    
    public DocumentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
