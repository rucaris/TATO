package com.tato.repository;

import com.tato.model.Review;
import com.tato.model.User;
import com.tato.model.Attraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

  List<Review> findByAttractionIdOrderByCreatedAtDesc(Long attractionId);

  List<Review> findByAttractionId(Long attractionId);

  @Query("SELECT r FROM Review r JOIN FETCH r.user WHERE r.attraction.id = :attractionId ORDER BY r.createdAt DESC")
  List<Review> findByAttractionIdWithUser(@Param("attractionId") Long attractionId);

  List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);

  Optional<Review> findByUserAndAttraction(User user, Attraction attraction);

  boolean existsByUserAndAttraction(User user, Attraction attraction);

  long countByAttractionId(Long attractionId);


  long countByUserId(Long userId);

  List<Review> findByRatingOrderByCreatedAtDesc(int rating);

  @Query("SELECT r FROM Review r JOIN FETCH r.user JOIN FETCH r.attraction ORDER BY r.createdAt DESC LIMIT :limit")
  List<Review> findTopNByOrderByCreatedAtDesc(@Param("limit") int limit);

  @Query("SELECT r FROM Review r WHERE r.createdAt >= :startDate AND r.createdAt <= :endDate ORDER BY r.createdAt DESC")
  List<Review> findByCreatedAtBetween(@Param("startDate") java.time.LocalDateTime startDate,
                                      @Param("endDate") java.time.LocalDateTime endDate);

  @Query("SELECT r FROM Review r JOIN FETCH r.user JOIN FETCH r.attraction WHERE r.rating <= :maxRating ORDER BY r.createdAt DESC")
  List<Review> findLowRatingReviews(@Param("maxRating") int maxRating);

  @Query("SELECT r FROM Review r JOIN FETCH r.user JOIN FETCH r.attraction WHERE r.content LIKE %:keyword% ORDER BY r.createdAt DESC")
  List<Review> findByContentContaining(@Param("keyword") String keyword);

  @Query("SELECT r.attraction.id, AVG(r.rating) FROM Review r GROUP BY r.attraction.id")
  List<Object[]> findAverageRatingByAttraction();

  @Query("SELECT r.rating, COUNT(r) FROM Review r GROUP BY r.rating ORDER BY r.rating")
  List<Object[]> countReviewsByRating();

  @Query("SELECT YEAR(r.createdAt), MONTH(r.createdAt), COUNT(r) FROM Review r GROUP BY YEAR(r.createdAt), MONTH(r.createdAt) ORDER BY YEAR(r.createdAt) DESC, MONTH(r.createdAt) DESC")
  List<Object[]> countReviewsByMonth();
}