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

  @GetMapping("/attractions/{id}")
  public String detail(@PathVariable String id, Model model, Principal principal) {
    log.debug("관광지 상세 페이지 요청: id={}", id);

    var attraction = attractionService.findByIdOrSpotId(id);
    if (attraction == null) {
      log.warn("관광지를 찾을 수 없습니다: id={}", id);
      return "redirect:/attractions";
    }

    log.info("관광지 상세 조회: spotId={}, 이름={}", attraction.getSpotId(), attraction.getName());

    model.addAttribute("attraction", attraction);
    model.addAttribute("reviews", reviewService.list(attraction.getId())); // 실제 DB ID 사용
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

  @PostMapping("/attractions/{id}/reviews")
  public String addReview(@PathVariable String id,
                          @RequestParam int rating,
                          @RequestParam String content,
                          Principal principal,
                          RedirectAttributes ra) {
    if (principal == null) {
      return "redirect:/login";
    }

    try {
      var attraction = attractionService.findByIdOrSpotId(id);
      if (attraction == null) {
        ra.addFlashAttribute("error", "관광지를 찾을 수 없습니다.");
        return "redirect:/attractions";
      }

      reviewService.addReview(attraction.getId(), content, rating); // 실제 DB ID 사용
      ra.addFlashAttribute("success", "리뷰가 등록되었습니다!");
    } catch (RuntimeException e) {
      ra.addFlashAttribute("error", "한 장소에 대해 하나의 리뷰만 가능합니다.");
    }

    return "redirect:/attractions/" + id;
  }
}