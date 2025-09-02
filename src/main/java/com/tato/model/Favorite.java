
package com.tato.model;
import jakarta.persistence.*;
import lombok.*;
@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(uniqueConstraints = @UniqueConstraint(columnNames={"username","attractionId"}))
public class Favorite {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String username;
  private Long attractionId;
}
