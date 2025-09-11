package com.tato.model;

import jakarta.persistence.*;
import lombok.*;
@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"latitude", "longitude"})
})
public class Attraction {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String name;
  private String nameKo;
  private String category;
  private String address;
  private String addressFullKo;
  private Double latitude;
  private Double longitude;
  @Column(length=2000) private String description;
  @Column(length=2000) private String descriptionKo;
  private String imageUrl; //이미지 삽입 위해 추가했습니다.
}