package com.microservice.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimStatusChangedEvent implements Serializable {
    private Long claimId;
    private String oldStatus;
    private String newStatus;
    private String timestamp;
}