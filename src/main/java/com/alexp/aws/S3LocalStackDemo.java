package com.alexp.aws;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.net.URI;
import java.nio.file.Paths;

/**
 * Demo application for AWS S3 with LocalStack
 * This example shows how to:
 * - Create a bucket
 * - Upload a file
 * - List objects
 * - Download a file
 */
public class S3LocalStackDemo {

    private static final String BUCKET_NAME = "demo-bucket";
    private static final String LOCALSTACK_ENDPOINT = "http://localhost:4566";

    public static void main(String[] args) {
        System.out.println("=== AWS S3 LocalStack Demo ===\n");

        // Create S3 client configured for LocalStack
        S3Client s3Client = createLocalStackS3Client();

        try {
            // 1. Create bucket
            createBucket(s3Client, BUCKET_NAME);

            // 2. Upload a file
            String fileName = "test-file.txt";
            String content = "Hello from LocalStack! This is a demo file.";
            uploadFile(s3Client, BUCKET_NAME, fileName, content);

            // 3. List all objects in the bucket
            listObjects(s3Client, BUCKET_NAME);

            // 4. Download and display the file
            downloadFile(s3Client, BUCKET_NAME, fileName);

            System.out.println("\n✓ Demo completed successfully!");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            s3Client.close();
        }
    }

    /**
     * Creates an S3 client configured for LocalStack
     */
    private static S3Client createLocalStackS3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(LOCALSTACK_ENDPOINT))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test", "test")))
                .forcePathStyle(true) // Required for LocalStack
                .build();
    }

    /**
     * Creates a new S3 bucket
     */
    private static void createBucket(S3Client s3Client, String bucketName) {
        try {
            CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build();

            s3Client.createBucket(createBucketRequest);
            System.out.println("✓ Bucket created: " + bucketName);

        } catch (S3Exception e) {
            if (e.statusCode() == 409) {
                System.out.println("✓ Bucket already exists: " + bucketName);
            } else {
                throw e;
            }
        }
    }

    /**
     * Uploads a file to S3
     */
    private static void uploadFile(S3Client s3Client, String bucketName, String key, String content) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromString(content));
        System.out.println("✓ File uploaded: " + key);
    }

    /**
     * Lists all objects in the bucket
     */
    private static void listObjects(S3Client s3Client, String bucketName) {
        System.out.println("\n--- Objects in bucket '" + bucketName + "': ---");

        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();

        ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);

        for (S3Object s3Object : listResponse.contents()) {
            System.out.println("  - " + s3Object.key() + " (Size: " + s3Object.size() + " bytes)");
        }
    }

    /**
     * Downloads and displays a file from S3
     */
    private static void downloadFile(S3Client s3Client, String bucketName, String key) {
        System.out.println("\n--- Downloading file: " + key + " ---");

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        String content = s3Client.getObjectAsBytes(getObjectRequest).asUtf8String();
        System.out.println("Content: " + content);
    }
}
