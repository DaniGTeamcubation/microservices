package com.microservices.ai.repository;

import com.microservices.ai.entity.DocumentClassification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentClassificationRepository extends JpaRepository<DocumentClassification, Long> {

    Optional<DocumentClassification> findByS3Key(String s3Key);

    Optional<DocumentClassification> findByDocumentId(Long documentId);
}
