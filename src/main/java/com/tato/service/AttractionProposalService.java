package com.tato.service;

import com.tato.model.Attraction;

import com.tato.model.AttractionProposal;
import com.tato.model.AttractionProposal.ProposalStatus;
import com.tato.repository.AttractionProposalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AttractionProposalService {

    private final AttractionProposalRepository attractionProposalRepository;
    private final AttractionService attractionService;

    public AttractionProposal submitProposal(AttractionProposal proposal) {
        proposal.setStatus(ProposalStatus.PENDING);
        proposal.setRequestDate(java.time.LocalDateTime.now());
        return attractionProposalRepository.save(proposal);
    }

    public List<AttractionProposal> findAllProposals() {
        return attractionProposalRepository.findAll();
    }

    public Optional<AttractionProposal> findProposalById(Long id) {
        return attractionProposalRepository.findById(id);
    }

    public List<AttractionProposal> findPendingProposals() {
        return attractionProposalRepository.findByStatus(ProposalStatus.PENDING);
    }

    public AttractionProposal updateProposalStatus(Long id, ProposalStatus status) {
        AttractionProposal proposal = attractionProposalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found with ID: " + id));
        proposal.setStatus(status);
        return attractionProposalRepository.save(proposal);
    }

    public Attraction approveProposal(Long id) {
        AttractionProposal proposal = attractionProposalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found with ID: " + id));

        if (proposal.getStatus() != ProposalStatus.PENDING) {
            throw new IllegalStateException("Proposal is not in PENDING status and cannot be approved.");
        }

        Attraction newAttraction = Attraction.builder()
                .name(proposal.getName())
                .category(proposal.getCategory())
                .address(proposal.getAddress())
                .latitude(proposal.getLatitude())
                .longitude(proposal.getLongitude())
                .description(proposal.getDescription())
                .build();

        Attraction savedAttraction = attractionService.createAttraction(newAttraction);

        proposal.setStatus(ProposalStatus.APPROVED);
        attractionProposalRepository.save(proposal);

        return savedAttraction;
    }

    public AttractionProposal rejectProposal(Long id) {
        AttractionProposal proposal = attractionProposalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found with ID: " + id));

        if (proposal.getStatus() != ProposalStatus.PENDING) {
            throw new IllegalStateException("Proposal is not in PENDING status and cannot be rejected.");
        }

        proposal.setStatus(ProposalStatus.REJECTED);
        return attractionProposalRepository.save(proposal);
    }
}