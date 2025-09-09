package com.tato.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

  @Transient
  public List<String> getCategoryList() {
    if (category == null || category.trim().isEmpty()) {
      return List.of("기타");
    }
    return Arrays.stream(category.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
  }

  public void setCategoryList(List<String> categories) {
    if (category == null || category.trim().isEmpty()) {
      this.category = "";
    } else {
      this.category =  categories.stream()
              .map(String::trim)
              .filter(s -> !s.isEmpty())
              .collect(Collectors.joining(","));
    }
  }

  public boolean hisCategory(String categoryName) {
    return getCategoryList().contains(categoryName.trim());
  }

  public String getMainCategory() {
    List<String> categories = getCategoryList();
    return categories.isEmpty() ? "기타" : categories.get(0);
  }
}