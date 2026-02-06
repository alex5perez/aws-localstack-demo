package com.alexp.aws;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for S3 operations with LocalStack
 * 
 * Prerequisites: LocalStack must be running on localhost:4566
 */
class S3LocalStackDemoTest {

    private static final String LOCALSTACK_ENDPOINT = "http://localhost:4566";
    private static final String TEST_BUCKET = "test-bucket";

    private S3Client createTestS3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(LOCALSTACK_ENDPOINT))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test", "test")))
                .forcePathStyle(true)
                .build();
    }

    @Test
    void testCreateBucketAndUploadFile() {
        S3Client s3Client = createTestS3Client();

        try {
            // Create bucket
            CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                    .bucket(TEST_BUCKET)
                    .build();

            s3Client.createBucket(createBucketRequest);

            // Upload file
            String testKey = "test-file.txt";
            String testContent = "Test content";

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(TEST_BUCKET)
                    .key(testKey)
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromString(testContent));

            // Verify file exists
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(TEST_BUCKET)
                    .key(testKey)
                    .build();

            HeadObjectResponse headResponse = s3Client.headObject(headRequest);
            assertNotNull(headResponse);

            // Download and verify content
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(TEST_BUCKET)
                    .key(testKey)
                    .build();

            String downloadedContent = s3Client.getObjectAsBytes(getRequest).asUtf8String();
            assertEquals(testContent, downloadedContent);

        } finally {
            s3Client.close();
        }
    }
}
