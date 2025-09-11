package com.tato.controller;

import com.tato.repository.UserRepository;
import com.tato.repository.AttractionRepository;
import com.tato.service.AttractionService;
import com.tato.service.ReviewService;
import com.tato.service.UserService;
import com.tato.model.Attraction;
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
                }
            }

            return "attraction-detail";
        } catch (NumberFormatException e) {
            log.error("잘못된 spotId 형식: {}", spotId, e);
            return "redirect:/attractions";
        }
    }

    // 리뷰 추가
    @PostMapping("/attractions/{spotId}/reviews")
    public String addReview(@PathVariable String spotId,
                            @RequestParam int rating,
                            @RequestParam String content,
                            Principal principal,
                            RedirectAttributes ra) {
        if (principal == null) return "redirect:/login";

        try {
            Long id = Long.parseLong(spotId);
            var attraction = attractionService.findById(id).orElse(null);

            if (attraction == null) {
                ra.addFlashAttribute("error","관광지를 찾을 수 없습니다.");
                return "redirect:/attractions";
            }

            reviewService.addReview(attraction.getId(), content, rating);
            ra.addFlashAttribute("success","리뷰가 등록되었습니다!");

        } catch (NumberFormatException e) {
            log.error("잘못된 spotId 형식: {}", spotId, e);
            ra.addFlashAttribute("error", "올바르지 않은 관광지 ID입니다.");
            return "redirect:/attractions";
        } catch (Exception e) {
            log.error("리뷰 등록 중 오류", e);
            ra.addFlashAttribute("error", "이미 리뷰를 작성했습니다.");
        }

        return "redirect:/attractions/" + spotId;
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