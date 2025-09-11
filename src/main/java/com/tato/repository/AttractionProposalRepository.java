package com.tato.repository;

import com.tato.model.AttractionProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import com.tato.model.AttractionProposal.ProposalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttractionProposalRepository extends JpaRepository<AttractionProposal, Long> {
    List<AttractionProposal> findByStatus(ProposalStatus status);
}