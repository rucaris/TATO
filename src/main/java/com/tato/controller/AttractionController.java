package com.tato.controller;

import com.tato.repository.UserRepository;
import com.tato.service.AttractionService;
import com.tato.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class AttractionController {

  private final AttractionService attractionService;
  private final ReviewService reviewService;
  private final UserRepository userRepository;

  @GetMapping("/attractions/{id}")
  public String detail(@PathVariable Long id, Model model, Principal principal) {
    var attraction = attractionService.findById(id).orElse(null);
    if (attraction == null) {
      return "redirect:/attractions";
    }

    model.addAttribute("attraction", attraction);
    model.addAttribute("reviews", reviewService.list(id));
    model.addAttribute("averageRating", reviewService.getAverageRating(id));

    // 로그인한 사용자 닉네임
    if (principal != null) {
      var user = userRepository.findByEmail(principal.getName()).orElse(null);
      if (user != null) {
        model.addAttribute("nickname", user.getNickname());
      }
    }

    return "attraction-detail";
  }

  @PostMapping("/attractions/{id}/reviews")
  public String addReview(@PathVariable Long id,
                          @RequestParam int rating,
                          @RequestParam String content,
                          Principal principal) {
    if (principal == null) {
      return "redirect:/login";
    }

    reviewService.addReview(id, content, rating);
    return "redirect:/attractions/" + id;
  }
}