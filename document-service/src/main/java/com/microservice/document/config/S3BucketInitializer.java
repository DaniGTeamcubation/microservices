package com.microservice.document.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Component
public class S3BucketInitializer {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public S3BucketInitializer(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @PostConstruct
    public void init() {
        try {
            s3Client.headBucket(
                    HeadBucketRequest.builder()
                            .bucket(bucketName)
                            .build()
            );
            log.info("✓ S3 bucket '{}' already exists", bucketName);
        } catch (S3Exception ex) {
            if (ex.statusCode() == 404) {
                log.warn("⚠ Bucket '{}' not found, creating it...", bucketName);
                try {
                    s3Client.createBucket(
                            CreateBucketRequest.builder()
                                    .bucket(bucketName)
                                    .build()
                    );
                    log.info("✓ Bucket '{}' created successfully", bucketName);
                } catch (Exception e) {
                    log.error("✗ Could not create bucket '{}'", bucketName, e);
                }
            } else {
                log.error("✗ Error checking bucket '{}'", bucketName, ex);
            }
        }
    }
}