package com.microservices.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassificationResponse {
    private Long id;
    private String s3Key;
    private Long documentId;
    private String category;
    private String subCategory;
    private Double confidence;
    private String summary;
    private String extractedInfo;
    private String status;
    private LocalDateTime classifiedAt;
    private String errorMessage;
}
