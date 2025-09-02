package com.tato.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name = "review")
public class Review {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // NEW: 로그인 사용자 FK
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "attraction_id", nullable = false)
  private Attraction attraction;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  private Integer rating;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  // getter/setter
  public Long getId() { return id; }
  public User getUser() { return user; }
  public void setUser(User user) { this.user = user; }
  public Attraction getAttraction() { return attraction; }
  public void setAttraction(Attraction attraction) { this.attraction = attraction; }
  public String getContent() { return content; }
  public void setContent(String content) { this.content = content; }
  public Integer getRating() { return rating; }
  public void setRating(Integer rating) { this.rating = rating; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
