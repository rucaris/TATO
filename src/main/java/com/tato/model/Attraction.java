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
  private String category;
  private String address;
  private Double latitude;
  private Double longitude;
  @Column(length=2000) private String description;
  @Column(name = "spot_id")
  private String spotId;
  private String imageUrl; //이미지 삽입 위해 추가했습니다.
  @Column(length=300) private String operatingHours;
  @Column(length=200) private String closedDays;
  @Column(length=500)
  private String website;

  public String getWebsite() { return website; }
  public void setWebsite(String website) { this.website = website; }

  @PrePersist
  @PreUpdate
  public void setSpotId() {
    if (this.spotId == null && this.id != null) {
      this.spotId = String.valueOf(this.id);
    }
  }
}