package com.alexp.aws.service;

import com.alexp.aws.exception.DocumentNotFoundException;
import com.alexp.aws.exception.InvalidFileException;
import com.alexp.aws.exception.S3OperationException;
import com.alexp.aws.model.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para DocumentService
 * 
 * Testing Strategy:
 * - Usa Mockito para mockear S3Client
 * - Tests de validaciones
 * - Tests de operaciones exitosas
 * - Tests de manejo de errores
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentService Tests")
class DocumentServiceTest {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private DocumentService documentService;

    @Captor
    private ArgumentCaptor<PutObjectRequest> putObjectRequestCaptor;

    @Captor
    private ArgumentCaptor<RequestBody> requestBodyCaptor;

    private static final String TEST_BUCKET = "test-bucket";

    @BeforeEach
    void setUp() {
        // Inyectar el nombre del bucket via reflection
        ReflectionTestUtils.setField(documentService, "bucketName", TEST_BUCKET);
    }

    // ========================================
    // TESTS DE VALIDACIONES
    // ========================================

    @Nested
    @DisplayName("File Validation Tests")
    class FileValidationTests {

        @Test
        @DisplayName("Should throw InvalidFileException when file is null")
        void shouldThrowExceptionWhenFileIsNull() {
            // Given
            MultipartFile file = null;
            String name = "Test Document";

            // When & Then
            assertThatThrownBy(() -> documentService.uploadDocument(file, name))
                    .isInstanceOf(InvalidFileException.class)
                    .hasMessageContaining("File cannot be null");
        }

        @Test
        @DisplayName("Should throw InvalidFileException when file is empty")
        void shouldThrowExceptionWhenFileIsEmpty() {
            // Given
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "file",
                    "empty.pdf",
                    "application/pdf",
                    new byte[0]
            );
            String name = "Test Document";

            // When & Then
            assertThatThrownBy(() -> documentService.uploadDocument(emptyFile, name))
                    .isInstanceOf(InvalidFileException.class)
                    .hasMessageContaining("File cannot be empty");
        }

        @Test
        @DisplayName("Should throw InvalidFileException when name is empty")
        void shouldThrowExceptionWhenNameIsEmpty() {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test.pdf",
                    "application/pdf",
                    "test content".getBytes()
            );
            String emptyName = "";

            // When & Then
            assertThatThrownBy(() -> documentService.uploadDocument(file, emptyName))
                    .isInstanceOf(InvalidFileException.class)
                    .hasMessageContaining("Document name cannot be empty");
        }

        @Test
        @DisplayName("Should throw InvalidFileException when file exceeds 10MB")
        void shouldThrowExceptionWhenFileTooLarge() {
            // Given - File de 11MB
            byte[] largeContent = new byte[11 * 1024 * 1024];
            MockMultipartFile largeFile = new MockMultipartFile(
                    "file",
                    "large.pdf",
                    "application/pdf",
                    largeContent
            );
            String name = "Large Document";

            // When & Then
            assertThatThrownBy(() -> documentService.uploadDocument(largeFile, name))
                    .isInstanceOf(InvalidFileException.class)
                    .hasMessageContaining("exceeds maximum allowed size");
        }

        @Test
        @DisplayName("Should throw InvalidFileException when content type not allowed")
        void shouldThrowExceptionWhenContentTypeNotAllowed() {
            // Given - Archivo .exe (no permitido)
            MockMultipartFile executableFile = new MockMultipartFile(
                    "file",
                    "virus.exe",
                    "application/x-msdownload",
                    "malicious content".getBytes()
            );
            String name = "Executable";

            // When & Then
            assertThatThrownBy(() -> documentService.uploadDocument(executableFile, name))
                    .isInstanceOf(InvalidFileException.class)
                    .hasMessageContaining("File type")
                    .hasMessageContaining("is not allowed");
        }

        @Test
        @DisplayName("Should accept valid PDF file")
        void shouldAcceptValidPdfFile() {
            // Given
            MockMultipartFile pdfFile = new MockMultipartFile(
                    "file",
                    "document.pdf",
                    "application/pdf",
                    "PDF content".getBytes()
            );
            String name = "Valid PDF";

            // Mock S3 operations
            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(PutObjectResponse.builder().build());

            // When & Then - No exception
            assertThatCode(() -> documentService.uploadDocument(pdfFile, name))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should accept valid image file")
        void shouldAcceptValidImageFile() {
            // Given
            MockMultipartFile imageFile = new MockMultipartFile(
                    "file",
                    "photo.jpg",
                    "image/jpeg",
                    "JPEG content".getBytes()
            );
            String name = "Valid Image";

            // Mock S3 operations
            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(PutObjectResponse.builder().build());

            // When & Then
            assertThatCode(() -> documentService.uploadDocument(imageFile, name))
                    .doesNotThrowAnyException();
        }
    }

    // ========================================
    // TESTS DE OPERACIONES EXITOSAS
    // ========================================

    @Nested
    @DisplayName("Document Upload Tests")
    class DocumentUploadTests {

        @Test
        @DisplayName("Should upload document successfully and return Document with all fields")
        void shouldUploadDocumentSuccessfully() {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "contract.pdf",
                    "application/pdf",
                    "Contract content here".getBytes()
            );
            String name = "Client Contract";

            // Mock S3 response
            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(PutObjectResponse.builder().build());

            // When
            Document result = documentService.uploadDocument(file, name);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            assertThat(result.getName()).isEqualTo(name);
            assertThat(result.getFileName()).contains("contract.pdf");
            assertThat(result.getContentType()).isEqualTo("application/pdf");
            assertThat(result.getSize()).isEqualTo(file.getSize());
            assertThat(result.getUploadDate()).isNotNull();
            assertThat(result.getS3Key()).startsWith("documents/");

            // Verify S3 interaction
            verify(s3Client).putObject(
                    putObjectRequestCaptor.capture(),
                    any(RequestBody.class)
            );

            PutObjectRequest capturedRequest = putObjectRequestCaptor.getValue();
            assertThat(capturedRequest.bucket()).isEqualTo(TEST_BUCKET);
            assertThat(capturedRequest.key()).startsWith("documents/");
        }

        @Test
        @DisplayName("Should generate unique S3 keys for multiple uploads")
        void shouldGenerateUniqueS3Keys() {
            // Given
            MockMultipartFile file1 = new MockMultipartFile(
                    "file", "doc.pdf", "application/pdf", "content1".getBytes()
            );
            MockMultipartFile file2 = new MockMultipartFile(
                    "file", "doc.pdf", "application/pdf", "content2".getBytes()
            );

            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(PutObjectResponse.builder().build());

            // When
            Document doc1 = documentService.uploadDocument(file1, "Doc 1");
            Document doc2 = documentService.uploadDocument(file2, "Doc 2");

            // Then
            assertThat(doc1.getS3Key()).isNotEqualTo(doc2.getS3Key());
            assertThat(doc1.getId()).isNotEqualTo(doc2.getId());
        }
    }

    @Nested
    @DisplayName("Document List Tests")
    class DocumentListTests {

        @Test
        @DisplayName("Should return empty list when bucket is empty")
        void shouldReturnEmptyListWhenBucketEmpty() {
            // Given
            ListObjectsV2Response emptyResponse = ListObjectsV2Response.builder()
                    .contents(List.of())
                    .build();

            when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
                    .thenReturn(emptyResponse);

            // When
            List<Document> result = documentService.listDocuments();

            // Then
            assertThat(result).isEmpty();
            verify(s3Client).listObjectsV2(any(ListObjectsV2Request.class));
        }

        @Test
        @DisplayName("Should return list of documents from S3")
        void shouldReturnDocumentsList() {
            // Given
            S3Object obj1 = S3Object.builder()
                    .key("documents/abc-123-file1.pdf")
                    .size(1024L)
                    .build();

            S3Object obj2 = S3Object.builder()
                    .key("documents/def-456-file2.docx")
                    .size(2048L)
                    .build();

            ListObjectsV2Response response = ListObjectsV2Response.builder()
                    .contents(Arrays.asList(obj1, obj2))
                    .build();

            when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
                    .thenReturn(response);

            // When
            List<Document> result = documentService.listDocuments();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getFileName()).isEqualTo("abc-123-file1.pdf");
            assertThat(result.get(1).getFileName()).isEqualTo("def-456-file2.docx");
        }
    }

    @Nested
    @DisplayName("Document Download Tests")
    class DocumentDownloadTests {

        @Test
        @DisplayName("Should throw DocumentNotFoundException when document not exists")
        void shouldThrowExceptionWhenDocumentNotFound() {
            // Given
            String nonExistentId = "non-existent-id";

            when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
                    .thenReturn(ListObjectsV2Response.builder().contents(List.of()).build());

            // When & Then
            assertThatThrownBy(() -> documentService.downloadDocument(nonExistentId))
                    .isInstanceOf(DocumentNotFoundException.class)
                    .hasMessageContaining(nonExistentId);
        }

        @Test
        @DisplayName("Should download document successfully")
        void shouldDownloadDocumentSuccessfully() {
            // Given
            String documentId = "test-id-123";
            String s3Key = "documents/" + documentId + "-file.pdf";
            byte[] fileContent = "PDF content".getBytes();

            // Mock listObjects
            S3Object s3Object = S3Object.builder()
                    .key(s3Key)
                    .size((long) fileContent.length)
                    .build();

            when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
                    .thenReturn(ListObjectsV2Response.builder()
                            .contents(List.of(s3Object))
                            .build());

            // Mock getObject
            ResponseInputStream mockInputStream = new ResponseInputStream<>(
                    GetObjectResponse.builder().build(),
                    new ByteArrayInputStream(fileContent)
            );

            when(s3Client.getObject(any(GetObjectRequest.class)))
                    .thenReturn(mockInputStream);

            // When
            byte[] result = documentService.downloadDocument(documentId);

            // Then
            assertThat(result).isEqualTo(fileContent);
            verify(s3Client).getObject(any(GetObjectRequest.class));
        }
    }

    @Nested
    @DisplayName("Document Delete Tests")
    class DocumentDeleteTests {

        @Test
        @DisplayName("Should throw DocumentNotFoundException when deleting non-existent document")
        void shouldThrowExceptionWhenDeletingNonExistentDocument() {
            // Given
            String nonExistentId = "non-existent";

            when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
                    .thenReturn(ListObjectsV2Response.builder().contents(List.of()).build());

            // When & Then
            assertThatThrownBy(() -> documentService.deleteDocument(nonExistentId))
                    .isInstanceOf(DocumentNotFoundException.class);
        }

        @Test
        @DisplayName("Should delete document successfully")
        void shouldDeleteDocumentSuccessfully() {
            // Given
            String documentId = "test-id";
            String s3Key = "documents/" + documentId + "-file.pdf";

            S3Object s3Object = S3Object.builder()
                    .key(s3Key)
                    .build();

            when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
                    .thenReturn(ListObjectsV2Response.builder()
                            .contents(List.of(s3Object))
                            .build());

            when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                    .thenReturn(DeleteObjectResponse.builder().build());

            // When
            assertThatCode(() -> documentService.deleteDocument(documentId))
                    .doesNotThrowAnyException();

            // Then
            verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
        }
    }

    // ========================================
    // TESTS DE MANEJO DE ERRORES S3
    // ========================================

    @Nested
    @DisplayName("S3 Error Handling Tests")
    class S3ErrorHandlingTests {

        @Test
        @DisplayName("Should throw S3OperationException when S3 upload fails")
        void shouldThrowS3ExceptionWhenUploadFails() {
            // Given
            MockMultipartFile file = new MockMultipartFile(
                    "file", "doc.pdf", "application/pdf", "content".getBytes()
            );

            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenThrow(S3Exception.builder().message("S3 Error").build());

            // When & Then
            assertThatThrownBy(() -> documentService.uploadDocument(file, "Test"))
                    .isInstanceOf(S3OperationException.class)
                    .hasMessageContaining("Failed to upload");
        }

        @Test
        @DisplayName("Should throw S3OperationException when listing fails")
        void shouldThrowS3ExceptionWhenListFails() {
            // Given
            when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
                    .thenThrow(S3Exception.builder().message("List error").build());

            // When & Then
            assertThatThrownBy(() -> documentService.listDocuments())
                    .isInstanceOf(S3OperationException.class);
        }
    }
}
