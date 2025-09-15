package com.tato.controller;

import com.tato.repository.UserRepository;
import com.tato.repository.AttractionRepository;
import com.tato.service.AttractionService;
import com.tato.service.ReviewService;
import com.tato.service.UserService;
import com.tato.model.Attraction;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AttractionController {

    private final AttractionService attractionService;
    private final ReviewService reviewService;
    private final UserRepository userRepository;
    private final AttractionRepository attractionRepository;
    private final UserService userService;

    // 관광지 상세 페이지
    @GetMapping("/attractions/{spotId}")
    public String detail(@PathVariable String spotId, Model model, Principal principal) {
        try {
            // String을 Long으로 변환
            Long id = Long.parseLong(spotId);
            var attraction = attractionService.findById(id).orElse(null);

            if (attraction == null) {
                return "redirect:/attractions";
            }

            model.addAttribute("attraction", attraction);
            model.addAttribute("reviews", reviewService.list(attraction.getId()));
            model.addAttribute("averageRating", reviewService.getAverageRating(attraction.getId()));

            // 로그인한 사용자 닉네임
            if (principal != null) {
                var user = userRepository.findByEmail(principal.getName()).orElse(null);
                if (user != null) {
                    model.addAttribute("nickname", user.getNickname());
                    model.addAttribute("userEmail", user.getEmail());
                    model.addAttribute("isAdmin", user.getRole().equals("ADMIN"));

                    boolean hasReviewed = reviewService.hasUserReviewedAttraction(user, attraction);
                    model.addAttribute("hasReviewed", hasReviewed);

                    if (hasReviewed) {
                        var existingReview = reviewService.findByUserAndAttraction(user, attraction);
                        model.addAttribute("userReview", existingReview.orElse(null));
                    }
                }
            }

            return "attraction-detail";
        } catch (NumberFormatException e) {
            log.error("잘못된 spotId 형식: {}", spotId, e);
            return "redirect:/attractions";
        }
    }

    // API 엔드포인트 - 모든 관광지 조회 (index.html에서 사용)
    @GetMapping("/api/attractions")
    @ResponseBody
    public ResponseEntity<List<Attraction>> getAllAttractions() {
        try {
            List<Attraction> attractions = attractionRepository.findAll();
            log.debug("API 요청으로 {} 개의 관광지 데이터 반환", attractions.size());
            return ResponseEntity.ok(attractions);
        } catch (Exception e) {
            log.error("관광지 데이터 조회 중 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}