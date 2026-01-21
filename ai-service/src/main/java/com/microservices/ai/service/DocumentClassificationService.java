package com.microservices.ai.service;

import com.microservices.ai.client.DocumentClient;
import com.microservices.ai.config.AiConfig;
import com.microservices.ai.dto.ClassificationResponse;
import com.microservices.ai.entity.DocumentClassification;
import com.microservices.ai.repository.DocumentClassificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class DocumentClassificationService {

    private final DocumentClassificationRepository classificationRepository;
    private final DocumentClient documentClient;
    private final OpenRouterService openRouterService;
    private final S3Client s3Client;
    private final AiConfig aiConfig;

    private static final String CLASSIFICATION_SYSTEM_PROMPT = """
            You are a medical document classifier AI assistant.
            Your task is to analyze medical documents and classify them into categories.
            
            Available categories:
            - MEDICAL_RECORD: Patient medical history, diagnoses, treatments
            - LAB_RESULT: Laboratory test results, blood work, imaging reports
            - PRESCRIPTION: Medication prescriptions and pharmacy documents
            - INSURANCE_CLAIM: Insurance claim forms and related documents
            - INVOICE: Medical bills and invoices
            - CONSENT_FORM: Patient consent and authorization forms
            - OTHER: Documents that don't fit other categories
            
            Respond ONLY with a JSON object in this exact format:
            {
              "category": "CATEGORY_NAME",
              "subCategory": "specific type",
              "confidence": 0.95,
              "summary": "brief summary of document content",
              "extractedInfo": "key information found in the document"
            }
            
            Do not include any other text, explanations, or markdown formatting.
            """;

    public DocumentClassificationService(
            DocumentClassificationRepository classificationRepository,
            DocumentClient documentClient,
            OpenRouterService openRouterService,
            S3Client s3Client,
            AiConfig aiConfig) {
        this.classificationRepository = classificationRepository;
        this.documentClient = documentClient;
        this.openRouterService = openRouterService;
        this.s3Client = s3Client;
        this.aiConfig = aiConfig;
    }

    public ClassificationResponse classifyDocument(String s3Key) {
        log.info("Starting classification for document: {}", s3Key);

        DocumentClassification classification = classificationRepository
                .findByS3Key(s3Key)
                .orElse(new DocumentClassification());

        classification.setS3Key(s3Key);
        classification.setStatus(DocumentClassification.ClassificationStatus.PROCESSING);
        classification = classificationRepository.save(classification);

        try {
            String documentContent = fetchDocumentFromS3(s3Key);

            String userMessage = String.format(
                    "Please analyze and classify this medical document:\n\n%s",
                    documentContent
            );

            String aiResponse = openRouterService.chatCompletion(
                    CLASSIFICATION_SYSTEM_PROMPT,
                    userMessage
            );

            updateClassificationFromAiResponse(classification, aiResponse);
            classification.setStatus(DocumentClassification.ClassificationStatus.COMPLETED);
            classification.setClassifiedAt(LocalDateTime.now());

            log.info("Classification completed successfully for: {}", s3Key);

        } catch (Exception e) {
            log.error("Error classifying document: {}", s3Key, e);
            classification.setStatus(DocumentClassification.ClassificationStatus.FAILED);
            classification.setErrorMessage(e.getMessage());
        }

        classification = classificationRepository.save(classification);
        return mapToResponse(classification);
    }

    public Optional<ClassificationResponse> getClassificationByS3Key(String s3Key) {
        return classificationRepository.findByS3Key(s3Key)
                .map(this::mapToResponse);
    }

    public Optional<ClassificationResponse> getClassificationByDocumentId(Long documentId) {
        return classificationRepository.findByDocumentId(documentId)
                .map(this::mapToResponse);
    }

    private String fetchDocumentFromS3(String s3Key) throws IOException {
        log.info("Fetching document from S3: {}", s3Key);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(aiConfig.getBucketName())
                .key(s3Key)
                .build();

        ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
        String content = new String(objectBytes.asByteArray(), StandardCharsets.UTF_8);

        log.info("Document fetched successfully. Size: {} bytes", content.length());
        return content;
    }

    private void updateClassificationFromAiResponse(
            DocumentClassification classification,
            String aiResponse) {

        try {
            aiResponse = aiResponse.trim();
            if (aiResponse.startsWith("```json")) {
                aiResponse = aiResponse.substring(7);
            }
            if (aiResponse.endsWith("```")) {
                aiResponse = aiResponse.substring(0, aiResponse.length() - 3);
            }
            aiResponse = aiResponse.trim();

            com.fasterxml.jackson.databind.ObjectMapper mapper =
                    new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> result = mapper.readValue(aiResponse, Map.class);

            classification.setCategory((String) result.get("category"));
            classification.setSubCategory((String) result.get("subCategory"));
            classification.setConfidence(((Number) result.get("confidence")).doubleValue());
            classification.setSummary((String) result.get("summary"));
            classification.setExtractedInfo((String) result.get("extractedInfo"));

        } catch (Exception e) {
            log.error("Error parsing AI response", e);
            classification.setCategory("UNKNOWN");
            classification.setConfidence(0.0);
            classification.setSummary(aiResponse);
        }
    }

    private ClassificationResponse mapToResponse(DocumentClassification classification) {
        return ClassificationResponse.builder()
                .id(classification.getId())
                .s3Key(classification.getS3Key())
                .documentId(classification.getDocumentId())
                .category(classification.getCategory())
                .subCategory(classification.getSubCategory())
                .confidence(classification.getConfidence())
                .summary(classification.getSummary())
                .extractedInfo(classification.getExtractedInfo())
                .status(classification.getStatus().name())
                .classifiedAt(classification.getClassifiedAt())
                .errorMessage(classification.getErrorMessage())
                .build();
    }
}