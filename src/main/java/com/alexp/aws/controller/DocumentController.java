package com.alexp.aws.controller;

import com.alexp.aws.model.Document;
import com.alexp.aws.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Controlador REST para gestión de documentos
 * 
 * Anotaciones:
 * - @RestController: Marca esta clase como controlador REST
 *   Combina @Controller + @ResponseBody (devuelve JSON automáticamente)
 * - @RequestMapping: Define la ruta base para todos los endpoints (/api/documents)
 * - @RequiredArgsConstructor: Lombok genera constructor con dependencias
 * - @Slf4j: Logger automático
 * 
 * Este controller expone 4 endpoints:
 * 1. POST /api/documents - Subir documento
 * 2. GET /api/documents - Listar todos los documentos
 * 3. GET /api/documents/{id}/download - Descargar documento
 * 4. DELETE /api/documents/{id} - Eliminar documento
 */
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final DocumentService documentService;

    /**
     * Endpoint: POST /api/documents
     * Sube un nuevo documento a S3
     * 
     * @param file Archivo a subir (viene en el body como multipart/form-data)
     * @param name Nombre descriptivo del documento
     * @return ResponseEntity con el documento creado y status 201 (CREATED)
     * 
     * Ejemplo de uso con curl:
     * curl -X POST http://localhost:8080/api/documents \
     *   -F "file=@documento.pdf" \
     *   -F "name=Mi Documento"
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Document> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name) {
        
        log.info("Received upload request: name={}, fileName={}, size={}", 
                name, file.getOriginalFilename(), file.getSize());

        try {
            // Validaciones básicas
            if (file.isEmpty()) {
                log.warn("Empty file received");
                return ResponseEntity.badRequest().build();
            }

            Document document = documentService.uploadDocument(file, name);
            log.info("Document uploaded successfully: {}", document.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(document);
            
        } catch (IOException e) {
            log.error("Error uploading document", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint: GET /api/documents
     * Lista todos los documentos almacenados
     * 
     * @return Lista de documentos con sus metadatos
     * 
     * Ejemplo de uso con curl:
     * curl http://localhost:8080/api/documents
     */
    @GetMapping
    public ResponseEntity<List<Document>> listDocuments() {
        log.info("Received list documents request");
        
        List<Document> documents = documentService.listDocuments();
        log.info("Returning {} documents", documents.size());
        
        return ResponseEntity.ok(documents);
    }

    /**
     * Endpoint: GET /api/documents/{id}/download
     * Descarga un documento específico
     * 
     * @param id ID del documento a descargar
     * @return Archivo como ByteArrayResource
     * 
     * Ejemplo de uso con curl:
     * curl http://localhost:8080/api/documents/abc-123-def/download -o archivo.pdf
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<ByteArrayResource> downloadDocument(@PathVariable String id) {
        log.info("Received download request for document: {}", id);
        
        try {
            byte[] data = documentService.downloadDocument(id);
            ByteArrayResource resource = new ByteArrayResource(data);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"document-" + id + "\"")
                    .contentLength(data.length)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
                    
        } catch (RuntimeException e) {
            log.error("Error downloading document: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Endpoint: DELETE /api/documents/{id}
     * Elimina un documento
     * 
     * @param id ID del documento a eliminar
     * @return ResponseEntity con status 204 (NO_CONTENT) si se eliminó correctamente
     * 
     * Ejemplo de uso con curl:
     * curl -X DELETE http://localhost:8080/api/documents/abc-123-def
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable String id) {
        log.info("Received delete request for document: {}", id);
        
        try {
            documentService.deleteDocument(id);
            log.info("Document deleted successfully: {}", id);
            return ResponseEntity.noContent().build();
            
        } catch (RuntimeException e) {
            log.error("Error deleting document: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Endpoint de health check
     * GET /api/documents/health
     * 
     * Útil para verificar que el servicio está activo
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Document Management Service is running!");
    }
}
