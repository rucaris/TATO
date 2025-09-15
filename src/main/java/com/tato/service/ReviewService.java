package com.tato.service;

import com.tato.model.Review;
import com.tato.model.User;
import com.tato.model.Attraction;
import com.tato.repository.AttractionRepository;
import com.tato.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReviewService {

  private final ReviewRepository reviewRepository;
  private final AttractionRepository attractionRepository;

  public List<Review> listWithUser(Long attractionId) {
    return reviewRepository.findByAttractionIdWithUser(attractionId);
  }

  public List<Review> list(Long attractionId) {
    return reviewRepository.findByAttractionId(attractionId);
  }

  public Review findById(Long reviewId) {
    return reviewRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다: " + reviewId));
  }

  public Optional<Review> findByUserAndAttraction(User user, Attraction attraction) {
    return reviewRepository.findByUserAndAttraction(user, attraction);
  }

  public boolean hasUserReviewedAttraction(User user, Attraction attraction) {
    return reviewRepository.existsByUserAndAttraction(user, attraction);
  }

  public List<Review> findByUserId(Long userId) {
    return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId);
  }

  public double getAverageRating(Long attractionId) {
    List<Review> reviews = reviewRepository.findByAttractionId(attractionId);
    if (reviews.isEmpty()) {
      return 0.0;
    }

    double sum = reviews.stream()
            .mapToDouble(Review::getRating)
            .sum();

    return Math.round((sum / reviews.size()) * 10.0) / 10.0;
  }

  public long getReviewCount(Long attractionId) {
    return reviewRepository.countByAttractionId(attractionId);
  }

  @Transactional
  public Long addReview(Long attractionId, String content, int rating, User user) {
    Review review = new Review();
    review.setContent(content);
    review.setRating(rating);
    review.setUser(user);

    Attraction attraction = new Attraction();
    attraction.setId(attractionId);
    review.setAttraction(attraction);

    review.setCreatedAt(LocalDateTime.now());

    Review savedReview = reviewRepository.save(review);
    log.info("리뷰 작성 완료: 사용자={}, 관광지ID={}, 평점={}", user.getNickname(), attractionId, rating);

    return savedReview.getId();
  }


  @Transactional
  public void addReview(Long attractionId, String content, int rating) {

    throw new UnsupportedOperationException("User 정보 필요. addReiview(attractionId, content, rating, user)를 사용하세요.");
  }

  @Transactional
    public  void updateReview(Long reviewId, String content, int rating) {
    Review review = findById(reviewId);

    review.setContent(content);
    review.setRating(rating);

    reviewRepository.save(review);
    log.info("리뷰 수정 완료: 리뷰ID={}, 작성자={}", reviewId, review.getUser().getNickname());
  }

  @Transactional
  public void deleteReview(Long reviewId) {
    Review review = findById(reviewId);
    String userNickname = review.getUser().getNickname();
    Long attractionId = review.getAttraction().getId();

    reviewRepository.delete(review);
    log.info("리뷰 삭제 완료: 리뷰ID={}, 작성자={}, 관광지ID={}", reviewId, userNickname, attractionId);
  }

  public long getTotalReviewCount() {
    return reviewRepository.count();
  }

  public long getUserReviewCount(Long userId) {
    return reviewRepository.countByUserId(userId);
  }

  public List<Review> getRecentReviews(int limit) {
    return reviewRepository.findTopNByOrderByCreatedAtDesc(limit);
  }

  public List<Review> findByRating(int rating) {
    return reviewRepository.findByRatingOrderByCreatedAtDesc(rating);
  }

  public Map<Long, Double> getAttractionRatingStats() {
    return new HashMap<>();
  }
}