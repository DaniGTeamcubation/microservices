package com.microservice.document.controller;

import com.microservice.document.entity.Document;
import com.microservice.document.service.DocumentService;
import com.microservice.document.util.DocumentCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Document> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam DocumentCategory category,
            @RequestParam(required = false) Long claimId
    ) {
        log.info("Uploading file: {} for claim: {}", file.getOriginalFilename(), claimId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.upload(file, category, claimId));
    }

    @GetMapping
    public ResponseEntity<List<Document>> getAllDocuments() {
        return ResponseEntity.ok(documentService.findAll());
    }

    @GetMapping("/claim/{claimId}")
    public ResponseEntity<List<Document>> getDocumentsByClaimId(@PathVariable Long claimId) {
        log.info("Fetching documents for claim: {}", claimId);
        return ResponseEntity.ok(documentService.findByClaimId(claimId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocumentById(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.findById(id));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadById(@PathVariable Long id) {
        log.info("Downloading document with id: {}", id);
        return documentService.downloadById(id);
    }

    @GetMapping("/{id}/presigned-url")
    public ResponseEntity<String> getPresignedUrl(@PathVariable Long id) {
        log.info("Generating presigned URL for document id: {}", id);
        return ResponseEntity.ok(documentService.generatePresignedUrlById(id));
    }
}