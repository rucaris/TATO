package com.tato.controller;

import com.tato.model.AttractionProposal;
import com.tato.service.AttractionProposalService;
import com.tato.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/proposals")
public class AttractionProposalController {

    private final AttractionProposalService attractionProposalService;
    private final UserService userService;

    @PostMapping("/attractions")
    public ResponseEntity<AttractionProposal> submitAttractionProposal(
            @RequestBody AttractionProposal proposal,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = userService.findByEmail(userDetails.getUsername()).getId();
            proposal.setUserId(userId);

            AttractionProposal submittedProposal = attractionProposalService.submitProposal(proposal);
            return ResponseEntity.status(HttpStatus.CREATED).body(submittedProposal);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}