package com.microservices.ai.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;
import java.time.Duration;


@Configuration
@Slf4j
public class AiConfig {

    @Getter
    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Getter
    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    @Getter
    @Value("${ai.model:google/gemini-pro}")
    private String model;

    @Getter
    @Value("${ai.temperature:0.7}")
    private Double temperature;

    @Getter
    @Value("${ai.max-tokens:2000}")
    private Integer maxTokens;

    @Value("${aws.s3.endpoint}")
    private String s3Endpoint;

    @Getter
    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Bean
    public OkHttpClient okHttpClient() {
        log.info("Initializing OkHttp client for OpenRouter API");
        log.info("Base URL: {}", baseUrl);
        log.info("Model: {}", model);

        return new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(30))
                .readTimeout(Duration.ofSeconds(60))
                .writeTimeout(Duration.ofSeconds(60))
                .build();
    }

    @Bean
    public S3Client s3Client() {
        log.info("Configuring S3 client for document access");
        log.info("S3 Endpoint: {}", s3Endpoint);
        log.info("Bucket: {}", bucketName);

        return S3Client.builder()
                .endpointOverride(URI.create(s3Endpoint))
                .region(Region.US_EAST_1)
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create("test", "test")
                        )
                )
                .forcePathStyle(true)
                .build();
    }

}