package com.tato.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttractionProposal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String category;
    private String address;
    private Double latitude;
    private Double longitude;
    @Column(length = 2000)
    private String description;

    private Long userId;
    private java.time.LocalDateTime requestDate;

    @Enumerated(EnumType.STRING)
    private ProposalStatus status;

    public enum ProposalStatus {
        PENDING, APPROVED, REJECTED
    }

    
}