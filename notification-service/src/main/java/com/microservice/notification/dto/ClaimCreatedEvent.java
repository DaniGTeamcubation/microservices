package com.microservice.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimCreatedEvent implements Serializable {
    private Long claimId;
    private Long policyId;
    private Double amount;
    private String status;
    private String timestamp;
}