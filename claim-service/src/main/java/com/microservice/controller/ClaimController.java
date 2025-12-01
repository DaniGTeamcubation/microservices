package com.microservice.controller;

import com.microservice.client.DocumentClient;
import com.microservice.entity.Claim;
import com.microservice.service.ClaimService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/claims")
public class ClaimController {

    private final ClaimService claimService;
    private final DocumentClient documentClient;

    public ClaimController(ClaimService claimService, DocumentClient documentClient) {
        this.claimService = claimService;
        this.documentClient = documentClient;
    }

    @PostMapping
    public ResponseEntity<Claim> createClaim(@RequestBody Claim claim) {
        Claim createdClaim = claimService.createClaim(claim);
        return new ResponseEntity<>(createdClaim, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Claim> getClaim(@PathVariable Long id) {
        return claimService.getClaimById(id)
                .map(claim -> new ResponseEntity<>(claim, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<Claim>> getAllClaims() {
        List<Claim> claims = claimService.getAllClaims();
        return new ResponseEntity<>(claims, HttpStatus.OK);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Claim> updateClaimStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate) {
        String newStatus = statusUpdate.get("status");
        return claimService.updateClaimStatus(id, newStatus)
                .map(claim -> new ResponseEntity<>(claim, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/{claimId}/documents/upload")
    public ResponseEntity<Map<String, Object>> uploadDocumentToClaim(
            @PathVariable Long claimId,
            @RequestParam("file") MultipartFile file) {

        if (claimService.getClaimById(claimId).isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Map<String, Object> document = documentClient.uploadFile(file, claimId);
        return new ResponseEntity<>(document, HttpStatus.CREATED);
    }

    @GetMapping("/{claimId}/documents")
    public ResponseEntity<List<Map<String, Object>>> getClaimDocuments(@PathVariable Long claimId) {
        List<Map<String, Object>> documents = documentClient.getDocumentsByClaimId(claimId);
        return ResponseEntity.ok(documents);
    }
}
