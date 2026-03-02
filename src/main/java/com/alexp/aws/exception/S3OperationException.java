package com.alexp.aws.exception;

/**
 * Excepción lanzada cuando hay un error con S3
 * 
 * Casos de uso:
 * - No se puede conectar con S3/LocalStack
 * - Error al subir archivo
 * - Error al descargar archivo
 * - Bucket no existe y no se puede crear
 * 
 * Uso:
 * throw new S3OperationException("Failed to upload file to S3", e);
 */
public class S3OperationException extends RuntimeException {
    
    public S3OperationException(String message) {
        super(message);
    }
    
    public S3OperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
