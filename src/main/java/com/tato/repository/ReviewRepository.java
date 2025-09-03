package com.tato.repository;

import com.tato.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
  List<Review> findAllByUserIdOrderByCreatedAtDesc(Long userId);
  List<Review> findAllByAttractionId(Long attractionId);

  // 최신순으로 정렬될 수 있게 추가했습니다.
  List<Review> findAllByAttractionIdOrderByCreatedAtDesc(Long attractionId);
}