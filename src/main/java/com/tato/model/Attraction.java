package com.tato.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Attraction {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // CSV의 spot_id를 저장할 필드 추가
  @Column(name = "spot_id", unique = true)
  private String spotId;

  private String name;
  private String nameKo;
  private String category;
  private String address;
  private String addressFullKo;
  private Double latitude;
  private Double longitude;

  @Column(length=2000)
  private String description;

  @Column(length=2000)
  private String descriptionKo;

  private String imageUrl;
}