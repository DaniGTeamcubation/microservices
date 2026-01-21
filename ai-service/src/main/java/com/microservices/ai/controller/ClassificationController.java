package com.microservices.ai.controller;

import com.microservices.ai.dto.ClassificationResponse;
import com.microservices.ai.service.DocumentClassificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ia")
@Slf4j
public class ClassificationController {

    private final DocumentClassificationService classificationService;

    public ClassificationController(DocumentClassificationService classificationService) {
        this.classificationService = classificationService;
    }

    @PostMapping("/clasificar/{s3Key}")
    public ResponseEntity<ClassificationResponse> classifyDocument(
            @PathVariable String s3Key) {

        log.info("Received classification request for S3 key: {}", s3Key);

        ClassificationResponse response = classificationService.classifyDocument(s3Key);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/clasificacion/{s3Key}")
    public ResponseEntity<ClassificationResponse> getClassification(
            @PathVariable String s3Key) {

        log.info("Retrieving classification for S3 key: {}", s3Key);

        return classificationService.getClassificationByS3Key(s3Key)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/clasificacion/document/{documentId}")
    public ResponseEntity<ClassificationResponse> getClassificationByDocumentId(
            @PathVariable Long documentId) {

        log.info("Retrieving classification for document ID: {}", documentId);

        return classificationService.getClassificationByDocumentId(documentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}