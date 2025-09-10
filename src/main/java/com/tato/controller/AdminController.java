package com.tato.controller;

import com.tato.model.User;
import com.tato.service.AttractionService;
import com.tato.service.UserService;
import com.tato.service.AttractionProposalService;
import com.tato.model.AttractionProposal;
import com.tato.dto.StatusUpdateDto;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final AttractionService attractionService;
    private final AttractionProposalService attractionProposalService;

    @GetMapping("/admin")
    public String adminPage() {
        return "admin";
    }

    @GetMapping("/admin/users")
    public String adminUsersPage(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        return "admin/users";
    }

    @GetMapping("/admin/users/{id}")
    @ResponseBody
    public ResponseEntity<User> getUserDetails(@PathVariable Long id) {
        try {
            User user = userService.findUserById(id);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/admin/users/{id}/role")
    @ResponseBody
    public ResponseEntity<Void> updateUserRole(@PathVariable Long id, @RequestParam String role) {
        try {
            userService.updateUserRole(id, role);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/admin/users/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/admin/attractions")
    public String adminAttractionsPage(Model model) {
        model.addAttribute("attractions", attractionService.findAllAttractions());
        return "admin/attractions";
    }

    @GetMapping("/admin/settings")
    public String adminSettingsPage(Model model) {
        List<AttractionProposal> pendingRequests = attractionProposalService.findPendingProposals();
        model.addAttribute("pendingRequests", pendingRequests);
        return "admin/settings";
    }

    @GetMapping("/api/admin/attractions/{id}")
    @ResponseBody
    public ResponseEntity<com.tato.model.Attraction> getAttractionDetails(@PathVariable Long id) {
        try {
            com.tato.model.Attraction attraction = attractionService.findAttractionById(id);
            return ResponseEntity.ok(attraction);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/api/admin/attractions/{id}")
    @ResponseBody
    public ResponseEntity<Void> updateAttraction(@PathVariable Long id, @RequestBody com.tato.model.Attraction attraction) {
        try {
            attractionService.updateAttraction(id, attraction);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/api/admin/attractions")
    @ResponseBody
    public ResponseEntity<com.tato.model.Attraction> createAttraction(@RequestBody com.tato.model.Attraction attraction) {
        try {
            com.tato.model.Attraction createdAttraction = attractionService.createAttraction(attraction);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAttraction);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/api/admin/attractions/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteAttraction(@PathVariable Long id) {
        try {
            attractionService.deleteAttraction(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/api/admin/proposals")
    @ResponseBody
    public ResponseEntity<List<com.tato.model.AttractionProposal>> getAllAttractionProposals() {
        List<com.tato.model.AttractionProposal> proposals = attractionProposalService.findAllProposals();
        return ResponseEntity.ok(proposals);
    }

    @GetMapping("/api/admin/attraction-requests/{id}")
    @ResponseBody
    public ResponseEntity<AttractionProposal> getAttractionProposalDetails(@PathVariable Long id) {
        try {
            AttractionProposal proposal = attractionProposalService.findProposalById(id)
                                                .orElseThrow(() -> new IllegalArgumentException("Attraction Proposal not found with ID: " + id));
            return ResponseEntity.ok(proposal);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
        @PutMapping("/api/admin/attraction-requests/{id}/status")
    @ResponseBody
    public ResponseEntity<Void> updateProposalStatus(@PathVariable Long id, @RequestBody StatusUpdateDto statusUpdateDto) {
        try {
            if ("APPROVED".equals(statusUpdateDto.getStatus())) {
                attractionProposalService.approveProposal(id);
            } else if ("REJECTED".equals(statusUpdateDto.getStatus())) {
                attractionProposalService.rejectProposal(id);
            } else {
                return ResponseEntity.badRequest().body(null); // Invalid status
            }
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(null); // Or a more specific error message
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}