package com.tato.model;

import jakarta.persistence.*;

@Entity
@Table(
        name = "favorite",
        uniqueConstraints = @UniqueConstraint(name = "uq_favorite_user_attraction",
                columnNames = {"user_id", "attraction_id"})
)
public class Favorite {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // NEW: 로그인 사용자 FK
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "attraction_id", nullable = false)
  private Attraction attraction;

  // getter/setter
  public Long getId() { return id; }
  public User getUser() { return user; }
  public void setUser(User user) { this.user = user; }
  public Attraction getAttraction() { return attraction; }
  public void setAttraction(Attraction attraction) { this.attraction = attraction; }
}
