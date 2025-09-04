package com.tato.controller;

import com.tato.service.ImageService;
import com.tato.service.ReviewService;
import com.tato.service.UserService;
import com.tato.repository.AttractionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class HomeController {

  private final UserService userService;
  private final ReviewService reviewService;
  private final AttractionRepository attractionRepository;
  private final ImageService imageService;

  @GetMapping("/")
  public String home(Model model, Principal principal) {
    if (principal != null) {
      var user = userService.findByEmail(principal.getName());
      model.addAttribute("username", user.getNickname());
      model.addAttribute("userEmail", user.getEmail());
    }
    return "index";
  }

  @GetMapping("/attractions")
  public String attractions(Model model, Principal principal) {
    if (principal != null) {
      var user = userService.findByEmail(principal.getName());
      model.addAttribute("username", user.getNickname());
      model.addAttribute("userEmail", user.getEmail());
    }
    return "attractions";
  }

  @GetMapping("/api/attractions/metadata")
  @ResponseBody
  public ResponseEntity<Map<String, Object>> getAttractionsMetadata() {
    Map<String, Object> metadata = new HashMap<>();

    // 관광지별 평점 정보
    Map<Long, Double> ratings = new HashMap<>();
    Map<Long, String> images = new HashMap<>();

    // 모든 관광지 조회
    var attractions = attractionRepository.findAll();

    for (var attraction : attractions) {
      Long id = attraction.getId();

      // 평균 평점 계산
      double avgRating = reviewService.getAverageRating(id);
      ratings.put(id, avgRating);

      // 이미지 URL
      images.put(id, imageService.getImageUrl(id));
    }

    metadata.put("ratings", ratings);
    metadata.put("images", images);

    return ResponseEntity.ok(metadata);
  }
}