package com.microservice.controller;

import com.microservice.entity.Claim;
import com.microservice.service.ClaimService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/claims")
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
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
}
