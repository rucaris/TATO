
package com.tato.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Review {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private Long attractionId;
  private String author;      // 작성자 표시명
  private int rating;         // 1~5
  @Column(length=2000) private String content;
  private LocalDateTime createdAt;
}
