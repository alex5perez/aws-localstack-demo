package com.alexp.aws.exception;

/**
 * Excepción lanzada cuando un archivo no es válido
 * 
 * Casos de uso:
 * - Archivo vacío
 * - Tipo de archivo no permitido (ej: .exe)
 * - Tamaño excede el límite
 * - Nombre de archivo inválido
 * 
 * Uso:
 * throw new InvalidFileException("File size exceeds 10MB limit");
 */
public class InvalidFileException extends RuntimeException {
    
    public InvalidFileException(String message) {
        super(message);
    }
    
    public InvalidFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
