package com.microservice.service;

import com.microservice.dto.ClaimCreatedEvent;
import com.microservice.dto.ClaimStatusChangedEvent;
import com.microservice.entity.Claim;
import com.microservice.publisher.ClaimEventPublisher;
import com.microservice.repository.ClaimRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ClaimService {
    private final ClaimRepository claimRepository;

    private final ClaimEventPublisher eventPublisher;

    public ClaimService(ClaimRepository claimRepository, ClaimEventPublisher eventPublisher) {
        this.claimRepository = claimRepository;
        this.eventPublisher = eventPublisher;
    }

    public Claim createClaim(Claim claim) {
        Claim savedClaim = claimRepository.save(claim);

        ClaimCreatedEvent event = new ClaimCreatedEvent(
                savedClaim.getId(),
                savedClaim.getPolicyId(),
                savedClaim.getAmount(),
                savedClaim.getStatus(),
                LocalDateTime.now().toString()
        );
        eventPublisher.publishClaimCreated(event);

        return savedClaim;
    }

    public Optional<Claim> getClaimById(Long id) {
        return claimRepository.findById(id);
    }

    public List<Claim> getAllClaims() {
        return claimRepository.findAll();
    }

    public Optional<Claim> updateClaimStatus(Long id, String newStatus) {
        Optional<Claim> claimOpt = claimRepository.findById(id);
        if (claimOpt.isPresent()) {
            Claim claim = claimOpt.get();
            String oldStatus = claim.getStatus();
            claim.setStatus(newStatus);
            claimRepository.save(claim);

            ClaimStatusChangedEvent event = new ClaimStatusChangedEvent(
                    claim.getId(),
                    oldStatus,
                    newStatus,
                    LocalDateTime.now().toString()
            );
            eventPublisher.publishClaimStatusChanged(event);

            return Optional.of(claim);
        }
        return Optional.empty();
    }
}
