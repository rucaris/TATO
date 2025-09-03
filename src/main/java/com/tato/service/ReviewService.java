package com.tato.service;

import com.tato.model.Attraction;
import com.tato.model.Review;
import com.tato.model.User;
import com.tato.repository.AttractionRepository;
import com.tato.repository.ReviewRepository;
import com.tato.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

  private final ReviewRepository reviewRepository;
  private final UserRepository userRepository;
  private final AttractionRepository attractionRepository;

  @Transactional
  public Long addReview(Long attractionId, String content, int rating) {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userRepository.findByEmail(email).orElseThrow();
    Attraction attraction = attractionRepository.findById(attractionId).orElseThrow();

    // 🆕 코드로 중복 체크 (DB 제약조건 없이)
    List<Review> existingReviews = reviewRepository.findByUserIdAndAttractionId(user.getId(), attractionId);
    if (!existingReviews.isEmpty()) {
      throw new RuntimeException("이미 리뷰를 작성했습니다.");
    }

    Review r = new Review();
    r.setUser(user);
    r.setAttraction(attraction);
    r.setContent(content);
    r.setRating(rating);
    r.setCreatedAt(LocalDateTime.now());

    return reviewRepository.save(r).getId();
  }

  @Transactional(readOnly = true)
  public List<Review> list(Long attractionId) {
    return reviewRepository.findAllByAttractionIdOrderByCreatedAtDesc(attractionId);
  }

  // 평균 평점 계산
  @Transactional(readOnly = true)
  public double getAverageRating(Long attractionId) {
    List<Review> reviews = reviewRepository.findAllByAttractionId(attractionId);
    if (reviews.isEmpty()) return 0.0;

    return reviews.stream()
            .mapToInt(Review::getRating)
            .average()
            .orElse(0.0);
  }
}