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
    // 현재 로그인한 사용자 이메일
    String email = SecurityContextHolder.getContext().getAuthentication().getName();

    User user = userRepository.findByEmail(email).orElseThrow();
    Attraction attraction = attractionRepository.findById(attractionId).orElseThrow();

    Review r = new Review();
    r.setUser(user);                // FK
    r.setAttraction(attraction);    // FK
    r.setContent(content);
    r.setRating(rating);
    r.setCreatedAt(LocalDateTime.now());

    return reviewRepository.save(r).getId();
  }

  @Transactional(readOnly = true)
  public List<Review> list(Long attractionId) {
    return reviewRepository.findAllByAttractionId(attractionId);
  }
}
