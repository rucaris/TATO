package com.tato.controller;

import com.tato.repository.UserRepository;
import com.tato.service.AttractionService;
import com.tato.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AttractionController {

  private final AttractionService attractionService;
  private final ReviewService reviewService;
  private final UserRepository userRepository;

  @GetMapping("/attractions/{spotId}")
  public String detail(@PathVariable String spotId, Model model, Principal principal) {
    var attraction = attractionService.findByIdOrSpotId(spotId);
    if (attraction == null) return "redirect:/attractions";

    model.addAttribute("attraction", attraction);
    model.addAttribute("reviews", reviewService.list(attraction.getId()));
    model.addAttribute("averageRating", reviewService.getAverageRating(attraction.getId()));

    // 로그인한 사용자 닉네임
    if (principal != null) {
      var user = userRepository.findByEmail(principal.getName()).orElse(null);
      if (user != null) {
        model.addAttribute("nickname", user.getNickname());
      }
    }

    return "attraction-detail";
  }

  @PostMapping("/attractions/{spotId}/reviews")
  public String addReview(@PathVariable String spotId,
                          @RequestParam int rating,
                          @RequestParam String content,
                          Principal principal,
                          RedirectAttributes ra) {
    if (principal == null) return "redirect:/login";

    var attraction = attractionService.findByIdOrSpotId(spotId);
    if (attraction == null) { ra.addFlashAttribute("error","관광지를 찾을 수 없습니다."); return "redirect:/attractions"; }

    reviewService.addReview(attraction.getId(), content, rating); // 내부는 실제 PK 사용
    ra.addFlashAttribute("success","리뷰가 등록되었습니다!");
    return "redirect:/attractions/" + spotId;
  }
}