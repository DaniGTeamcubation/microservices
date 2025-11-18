package com.microservice.service;

import com.microservice.entity.Claim;
import com.microservice.repository.ClaimRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClaimService {

    private final ClaimRepository claimRepository;

    public ClaimService(ClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
    }

    public Claim createClaim(Claim claim) {
        return claimRepository.save(claim);
    }

    public Optional<Claim> getClaimById(Long id) {
        return claimRepository.findById(id);
    }

    public List<Claim> getAllClaims() {
        return claimRepository.findAll();
    }

    public Optional<Claim> updateClaimStatus(Long id, String status) {
        Optional<Claim> claimOpt = claimRepository.findById(id);
        if (claimOpt.isPresent()) {
            Claim claim = claimOpt.get();
            claim.setStatus(status);
            claimRepository.save(claim);
            return Optional.of(claim);
        }
        return Optional.empty();
    }
}
