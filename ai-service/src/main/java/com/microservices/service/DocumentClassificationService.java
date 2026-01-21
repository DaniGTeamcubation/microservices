package com.microservices.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.client.DocumentClient;
import com.microservices.dto.ClassificationResponse;
import com.microservices.entity.DocumentClassification;
import com.microservices.provider.AIProvider;
import com.microservices.repository.DocumentClassificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class DocumentClassificationService {

    private final DocumentClassificationRepository repository;
    private final DocumentClient documentClient;
    private final AIProvider aiProvider;
    private final S3Client s3Client;
    private final ObjectMapper objectMapper;
    private final String bucketName;

    private static final String CLASSIFICATION_SYSTEM_PROMPT = """
            You are a medical document classifier AI assistant.
            Your task is to analyze medical documents and classify them into categories.

            Available categories:
            - MEDICAL_RECORD
            - LAB_RESULT
            - PRESCRIPTION
            - INSURANCE_CLAIM
            - INVOICE
            - CONSENT_FORM
            - OTHER

            Respond ONLY with a JSON object in this exact format:
            {
              "category": "CATEGORY_NAME",
              "subCategory": "specific type",
              "confidence": 0.95,
              "summary": "brief summary",
              "extractedInfo": "key information"
            }
            """;

    public DocumentClassificationService(
            DocumentClassificationRepository repository,
            DocumentClient documentClient,
            AIProvider aiProvider,
            S3Client s3Client,
            ObjectMapper objectMapper,
            @Value("${ai.documents.bucket}") String bucketName) {

        this.repository = repository;
        this.documentClient = documentClient;
        this.aiProvider = aiProvider;
        this.s3Client = s3Client;
        this.objectMapper = objectMapper;
        this.bucketName = bucketName;
    }

    public ClassificationResponse classifyDocument(String s3Key) {
        log.info("Starting classification for document {}", s3Key);

        DocumentClassification classification = repository
                .findByS3Key(s3Key)
                .orElseGet(DocumentClassification::new);

        classification.setS3Key(s3Key);
        classification.setStatus(DocumentClassification.ClassificationStatus.PROCESSING);
        repository.save(classification);

        try {
            String documentContent = fetchDocumentFromS3(s3Key);

            String aiResponse = aiProvider.generateWithContext(
                    CLASSIFICATION_SYSTEM_PROMPT,
                    "Classify the following medical document:\n\n" + documentContent
            );

            applyAiResponse(classification, aiResponse);

            classification.setStatus(DocumentClassification.ClassificationStatus.COMPLETED);
            classification.setClassifiedAt(LocalDateTime.now());

        } catch (Exception e) {
            log.error("Document classification failed", e);
            classification.setStatus(DocumentClassification.ClassificationStatus.FAILED);
            classification.setErrorMessage(e.getMessage());
        }

        repository.save(classification);
        return mapToResponse(classification);
    }

    public Optional<ClassificationResponse> getClassificationByS3Key(String s3Key) {
        return repository.findByS3Key(s3Key).map(this::mapToResponse);
    }

    private String fetchDocumentFromS3(String s3Key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

        ResponseBytes<GetObjectResponse> bytes =
                s3Client.getObjectAsBytes(request);

        return new String(bytes.asByteArray(), StandardCharsets.UTF_8);
    }

    private void applyAiResponse(
            DocumentClassification classification,
            String aiResponse) throws Exception {

        String cleanJson = aiResponse
                .replace("```json", "")
                .replace("```", "")
                .trim();

        Map<String, Object> result =
                objectMapper.readValue(cleanJson, Map.class);

        classification.setCategory((String) result.get("category"));
        classification.setSubCategory((String) result.get("subCategory"));
        classification.setConfidence(((Number) result.get("confidence")).doubleValue());
        classification.setSummary((String) result.get("summary"));
        classification.setExtractedInfo((String) result.get("extractedInfo"));
    }

    private ClassificationResponse mapToResponse(DocumentClassification c) {
        return ClassificationResponse.builder()
                .id(c.getId())
                .s3Key(c.getS3Key())
                .documentId(c.getDocumentId())
                .category(c.getCategory())
                .subCategory(c.getSubCategory())
                .confidence(c.getConfidence())
                .summary(c.getSummary())
                .extractedInfo(c.getExtractedInfo())
                .status(c.getStatus().name())
                .classifiedAt(c.getClassifiedAt())
                .errorMessage(c.getErrorMessage())
                .build();
    }
}