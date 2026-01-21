package com.microservices.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_classifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentClassification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String s3Key;

    @Column(nullable = false)
    private Long documentId;

    private String category;
    private String subCategory;
    private Double confidence;

    @Column(length = 2000)
    private String summary;

    @Column(length = 5000)
    private String extractedInfo;

    @Enumerated(EnumType.STRING)
    private ClassificationStatus status;

    private LocalDateTime classifiedAt;

    @Column(length = 1000)
    private String errorMessage;

    public enum ClassificationStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED
    }

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = ClassificationStatus.PENDING;
        }
        if (classifiedAt == null && status == ClassificationStatus.COMPLETED) {
            classifiedAt = LocalDateTime.now();
        }
    }
}
