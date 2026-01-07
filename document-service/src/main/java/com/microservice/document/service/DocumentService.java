package com.microservice.document.service;

import com.microservice.document.entity.Document;
import com.microservice.document.exception.DocumentNotFoundException;
import com.microservice.document.exception.FileStorageException;
import com.microservice.document.repository.DocumentRepository;
import com.microservice.document.util.DocumentCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public Document upload(MultipartFile file,
                           DocumentCategory category,
                           Long claimId) {

        try {
            String s3Key = buildS3Key(category, claimId, file.getOriginalFilename());

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            Document document = new Document();
            document.setFilename(file.getOriginalFilename());
            document.setFileType(file.getContentType());
            document.setFileSize(file.getSize());
            document.setUploadDate(LocalDateTime.now());
            document.setS3Key(s3Key);
            document.setClaimId(claimId);

            Document saved = documentRepository.save(document);

            log.info("File uploaded to S3. bucket={}, key={}", bucketName, s3Key);
            return saved;

        } catch (Exception e) {
            log.error("Error uploading file to S3", e);
            throw new FileStorageException("Could not upload file to S3", e);
        }
    }


    public ResponseEntity<byte[]> download(String key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            ResponseBytes<GetObjectResponse> objectBytes =
                    s3Client.getObjectAsBytes(getObjectRequest);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + extractFilename(key) + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(objectBytes.asByteArray());

        } catch (NoSuchKeyException e) {
            throw new DocumentNotFoundException("File not found in S3: " + key);
        }
    }


    public String generatePresignedUrl(String key) {

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(15))
                        .getObjectRequest(getObjectRequest)
                        .build();

        PresignedGetObjectRequest presignedRequest =
                s3Presigner.presignGetObject(presignRequest);

        return presignedRequest.url().toString();
    }


    private String buildS3Key(DocumentCategory category,
                              Long claimId,
                              String originalFilename) {

        String uuid = UUID.randomUUID().toString();

        if (claimId != null) {
            return category.name().toLowerCase()
                    + "/claim-" + claimId
                    + "/" + uuid + "-" + originalFilename;
        }

        return category.name().toLowerCase()
                + "/" + uuid + "-" + originalFilename;
    }

    private String extractFilename(String key) {
        int index = key.lastIndexOf("/");
        return index >= 0 ? key.substring(index + 1) : key;
    }

    public Document findById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found: " + id));
    }

    public List<Document> findAll() {
        return documentRepository.findAll();
    }

    public List<Document> findByClaimId(Long claimId) {
        return documentRepository.findByClaimId(claimId);
    }

    public ResponseEntity<byte[]> downloadById(Long id) {
        Document document = findById(id);
        return download(document.getS3Key());
    }

    public String generatePresignedUrlById(Long id) {
        Document document = findById(id);
        return generatePresignedUrl(document.getS3Key());
    }
}