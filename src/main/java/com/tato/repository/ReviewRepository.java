package com.tato.repository;

import com.tato.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
  List<Review> findAllByUserIdOrderByCreatedAtDesc(Long userId);
  List<Review> findAllByAttractionId(Long attractionId);
}
