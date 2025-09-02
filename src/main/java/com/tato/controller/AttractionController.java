package com.tato.controller;

import com.tato.repository.UserRepository;
import com.tato.service.AttractionService;
import com.tato.service.FavoriteService;
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
  private final FavoriteService favoriteService;
  private final UserRepository userRepository; // 닉네임 표시용

  @GetMapping("/attractions/{id}")
  public String detail(@PathVariable Long id, Model model, Principal principal) {
    var attraction = attractionService.findById(id).orElse(null);
    model.addAttribute("attraction", attraction);

    // 리뷰 목록: FK 기반 (user_id) 저장, 조회는 attractionId로
    model.addAttribute("reviews", reviewService.list(id));

    // 찜 여부: 로그인 상태면 현재 사용자 기준으로 조회
    boolean isFav = principal != null && favoriteService.isFavorite(id);
    model.addAttribute("isFav", isFav);

    // 상단에 닉네임 노출(로그인 상태일 때)
    if (principal != null) {
      var nickname = userRepository.findByEmail(principal.getName())
              .map(u -> u.getNickname())
              .orElse(null);
      model.addAttribute("nickname", nickname);
    }

    return "attraction-detail";
  }

  @PostMapping("/attractions/{id}/reviews")
  public String addReview(@PathVariable Long id,
                          @RequestParam int rating,
                          @RequestParam String content) {
    // 현재 로그인 사용자는 서비스 내부에서 SecurityContext로 확인
    reviewService.addReview(id, content, rating);
    return "redirect:/attractions/" + id;
  }

  @PostMapping("/attractions/{id}/favorite")
  public String toggleFavorite(@PathVariable Long id) {
    // 현재 로그인 사용자는 서비스 내부에서 SecurityContext로 확인
    favoriteService.toggle(id);
    return "redirect:/attractions/" + id;
  }
}
