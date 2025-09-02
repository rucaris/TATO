
package com.tato.service;
import com.tato.model.Review;
import com.tato.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
@Service @RequiredArgsConstructor
public class ReviewService {
  private final ReviewRepository repo;
  public List<Review> list(Long attractionId){ return repo.findByAttractionIdOrderByCreatedAtDesc(attractionId); }
  public Review add(Long attractionId, String author, int rating, String content){
    Review r = Review.builder().attractionId(attractionId).author(author)
      .rating(rating).content(content).createdAt(LocalDateTime.now()).build();
    return repo.save(r);
  }
}
