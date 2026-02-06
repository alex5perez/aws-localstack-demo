package com.alexp.aws.service;

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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        }
    }

    /**
     * Sube un documento a S3
     * 
     * @param file Archivo a subir (MultipartFile viene del request HTTP)
     * @param name Nombre descriptivo del documento
     * @return Document con los metadatos del archivo subido
     * @throws IOException si hay error al leer el archivo
     */
    public Document uploadDocument(MultipartFile file, String name) throws IOException {
        // Generar ID único
        String documentId = UUID.randomUUID().toString();
        
        // Crear key en S3 (ruta del archivo)
        String s3Key = "documents/" + documentId + "-" + file.getOriginalFilename();
        
        log.info("Uploading document: {} to S3 key: {}", name, s3Key);

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
    }

    /**
     * Lista todos los documentos en el bucket
     * 
     * @return Lista de documentos con metadatos
     */
    public List<Document> listDocuments() {
        log.info("Listing all documents from bucket: {}", bucketName);

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
    }

    /**
     * Descarga un documento de S3
     * 
     * @param documentId ID del documento
     * @return Bytes del archivo
     */
    public byte[] downloadDocument(String documentId) {
        log.info("Downloading document: {}", documentId);

        // Buscar el documento por ID
        List<Document> documents = listDocuments();
        Document document = documents.stream()
                .filter(doc -> doc.getId().equals(documentId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));

        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(document.getS3Key())
                .build();

        byte[] data = s3Client.getObjectAsBytes(getRequest).asByteArray();
        log.info("Document downloaded: {} ({} bytes)", documentId, data.length);
        
        return data;
    }

    /**
     * Elimina un documento de S3
     * 
     * @param documentId ID del documento a eliminar
     */
    public void deleteDocument(String documentId) {
        log.info("Deleting document: {}", documentId);

        // Buscar el documento por ID
        List<Document> documents = listDocuments();
        Document document = documents.stream()
                .filter(doc -> doc.getId().equals(documentId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));

        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(document.getS3Key())
                .build();

        s3Client.deleteObject(deleteRequest);
        log.info("Document deleted: {}", documentId);
    }

    /**
     * Extrae el ID del nombre del archivo
     * Ejemplo: "abc-123-def-archivo.pdf" -> "abc-123-def"
     */
    private String extractIdFromKey(String fileName) {
        int dashIndex = fileName.indexOf('-');
        if (dashIndex > 0) {
            int lastDashIndex = fileName.lastIndexOf('-');
            if (lastDashIndex > dashIndex) {
                return fileName.substring(0, lastDashIndex);
            }
        }
        return fileName;
    }
}
