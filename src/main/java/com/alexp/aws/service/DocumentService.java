package com.alexp.aws.service;

import com.alexp.aws.exception.DocumentNotFoundException;
import com.alexp.aws.exception.InvalidFileException;
import com.alexp.aws.exception.S3OperationException;
import com.alexp.aws.model.Document;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Servicio de gestión de documentos
 * 
 * Anotaciones:
 * - @Service: Marca esta clase como un componente de servicio de Spring
 * - @RequiredArgsConstructor (Lombok): Genera constructor con campos 'final'
 * - @Slf4j (Lombok): Crea automáticamente un logger llamado 'log'
 * 
 * Este servicio contiene toda la lógica de negocio para gestionar documentos:
 * - Subir archivos a S3
 * - Listar documentos
 * - Descargar documentos
 * - Eliminar documentos
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    // Inyección de dependencias (Spring lo hace automáticamente)
    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    // ========== CONSTANTES DE VALIDACIÓN ==========
    
    /**
     * Tamaño máximo de archivo: 10 MB en bytes
     * 10 * 1024 * 1024 = 10,485,760 bytes
     */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    
    /**
     * Tipos de archivo permitidos (MIME types)
     * 
     * Documentos: PDF, Word, Excel, PowerPoint, Text
     * Imágenes: JPEG, PNG, GIF
     */
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
        // Documentos
        "application/pdf",                                                      // PDF
        "application/msword",                                                   // Word (.doc)
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // Word (.docx)
        "application/vnd.ms-excel",                                            // Excel (.xls)
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",  // Excel (.xlsx)
        "application/vnd.ms-powerpoint",                                       // PowerPoint (.ppt)
        "application/vnd.openxmlformats-officedocument.presentationml.presentation", // PowerPoint (.pptx)
        "text/plain",                                                          // Text
        
        // Imágenes
        "image/jpeg",                                                          // JPEG
        "image/png",                                                           // PNG
        "image/gif"                                                            // GIF
    );

    /**
     * Se ejecuta después de crear el bean
     * Crea el bucket si no existe
     */
    @PostConstruct
    public void init() {
        createBucketIfNotExists();
    }

    /**
     * Crea el bucket en S3 si no existe
     */
    private void createBucketIfNotExists() {
        try {
            // Verificar si el bucket existe
            s3Client.headBucket(HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build());
            log.info("Bucket '{}' already exists", bucketName);
        } catch (NoSuchBucketException e) {
            // El bucket no existe, lo creamos
            s3Client.createBucket(CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build());
            log.info("Bucket '{}' created successfully", bucketName);
        } catch (Exception e) {
            throw new S3OperationException("Failed to create or verify bucket: " + bucketName, e);
        }
    }

    /**
     * Valida un archivo antes de subirlo a S3
     * 
     * Validaciones:
     * 1. Archivo no puede ser null o vacío
     * 2. Tamaño no puede exceder MAX_FILE_SIZE (10MB)
     * 3. Tipo de archivo debe estar en ALLOWED_CONTENT_TYPES
     * 
     * @param file Archivo a validar
     * @param name Nombre descriptivo del documento
     * @throws InvalidFileException si el archivo no es válido
     */
    private void validateFile(MultipartFile file, String name) {
        // Validación 1: Archivo no puede ser null
        if (file == null) {
            throw new InvalidFileException("File cannot be null");
        }
        
        // Validación 2: Archivo no puede estar vacío
        if (file.isEmpty()) {
            throw new InvalidFileException("File cannot be empty");
        }
        
        // Validación 3: Nombre no puede ser vacío o null
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidFileException("Document name cannot be empty");
        }
        
        // Validación 4: Tamaño máximo
        if (file.getSize() > MAX_FILE_SIZE) {
            double sizeMB = file.getSize() / (1024.0 * 1024.0);
            throw new InvalidFileException(String.format(
                "File size (%.2f MB) exceeds maximum allowed size (10 MB)", sizeMB));
        }
        
        // Validación 5: Tipo de archivo permitido
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new InvalidFileException(String.format(
                "File type '%s' is not allowed. Allowed types: PDF, Word, Excel, PowerPoint, Images (JPEG, PNG, GIF)", 
                contentType));
        }
        
        // Validación 6: Nombre de archivo original no puede ser vacío
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new InvalidFileException("Original filename cannot be empty");
        }
        
        log.debug("File validation passed: {} ({})", name, contentType);
    }

    /**
     * Sube un documento a S3
     * 
     * @param file Archivo a subir (MultipartFile viene del request HTTP)
     * @param name Nombre descriptivo del documento
     * @return Document con los metadatos del archivo subido
     * @throws InvalidFileException si el archivo no pasa las validaciones
     * @throws S3OperationException si hay error al subir a S3
     */
    public Document uploadDocument(MultipartFile file, String name) {
        // Validar archivo ANTES de procesar
        validateFile(file, name);
        
        // Generar ID único
        String documentId = UUID.randomUUID().toString();
        
        // Crear key en S3 (ruta del archivo)
        String s3Key = "documents/" + documentId + "-" + file.getOriginalFilename();
        
        log.info("Uploading document: {} to S3 key: {}", name, s3Key);

        try {
            // Subir archivo a S3
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));
            
            log.info("Document uploaded successfully: {}", s3Key);

            // Crear objeto Document con metadatos
            return Document.builder()
                    .id(documentId)
                    .name(name)
                    .fileName(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .s3Key(s3Key)
                    .uploadDate(LocalDateTime.now())
                    .build();
                    
        } catch (IOException e) {
            log.error("Error reading file bytes: {}", name, e);
            throw new S3OperationException("Failed to read file: " + name, e);
        } catch (S3Exception e) {
            log.error("S3 error uploading document: {}", name, e);
            throw new S3OperationException("Failed to upload document to S3: " + name, e);
        }
    }

    /**
     * Lista todos los documentos en el bucket
     * 
     * @return Lista de documentos con metadatos
     * @throws S3OperationException si hay error al listar objetos de S3
     */
    public List<Document> listDocuments() {
        log.info("Listing all documents from bucket: {}", bucketName);

        try {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix("documents/")
                    .build();

            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
            List<Document> documents = new ArrayList<>();

            for (S3Object s3Object : listResponse.contents()) {
                // Extraer ID del nombre del archivo
                String key = s3Object.key();
                String fileName = key.substring(key.lastIndexOf('/') + 1);
                String documentId = extractIdFromKey(fileName);

                documents.add(Document.builder()
                        .id(documentId)
                        .fileName(fileName)
                        .s3Key(key)
                        .size(s3Object.size())
                        .uploadDate(LocalDateTime.now()) // En una app real, esto vendría de metadatos
                        .build());
            }

            log.info("Found {} documents", documents.size());
            return documents;
            
        } catch (S3Exception e) {
            log.error("S3 error listing documents", e);
            throw new S3OperationException("Failed to list documents from S3", e);
        }
    }

    /**
     * Descarga un documento de S3
     * 
     * @param documentId ID del documento
     * @return Bytes del archivo
     * @throws DocumentNotFoundException si el documento no existe
     * @throws S3OperationException si hay error al descargar de S3
     */
    public byte[] downloadDocument(String documentId) {
        log.info("Downloading document: {}", documentId);

        // Buscar el documento por ID
        List<Document> documents = listDocuments();
        Document document = documents.stream()
                .filter(doc -> doc.getId().equals(documentId))
                .findFirst()
                .orElseThrow(() -> new DocumentNotFoundException(
                    "Document not found with ID: " + documentId));

        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(document.getS3Key())
                    .build();

            byte[] data = s3Client.getObjectAsBytes(getRequest).asByteArray();
            log.info("Document downloaded: {} ({} bytes)", documentId, data.length);
            
            return data;
            
        } catch (S3Exception e) {
            log.error("S3 error downloading document: {}", documentId, e);
            throw new S3OperationException("Failed to download document: " + documentId, e);
        }
    }

    /**
     * Elimina un documento de S3
     * 
     * @param documentId ID del documento a eliminar
     * @throws DocumentNotFoundException si el documento no existe
     * @throws S3OperationException si hay error al eliminar de S3
     */
    public void deleteDocument(String documentId) {
        log.info("Deleting document: {}", documentId);

        // Buscar el documento por ID
        List<Document> documents = listDocuments();
        Document document = documents.stream()
                .filter(doc -> doc.getId().equals(documentId))
                .findFirst()
                .orElseThrow(() -> new DocumentNotFoundException(
                    "Document not found with ID: " + documentId));

        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(document.getS3Key())
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("Document deleted: {}", documentId);
            
        } catch (S3Exception e) {
            log.error("S3 error deleting document: {}", documentId, e);
            throw new S3OperationException("Failed to delete document: " + documentId, e);
        }
    }

    /**
     * Extrae el ID del nombre del archivo
     * Ejemplo: "eaf1811c-b4ce-4417-87c7-c5177000e933-test-upload.txt" -> "eaf1811c-b4ce-4417-87c7-c5177000e933"
     * El UUID siempre tiene 36 caracteres (formato: 8-4-4-4-12)
     */
    private String extractIdFromKey(String fileName) {
        // El UUID siempre son los primeros 36 caracteres
        if (fileName.length() >= 36) {
            return fileName.substring(0, 36);
        }
        return fileName;
    }
}
