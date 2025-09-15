package com.tato.controller;

import com.tato.model.User;
import com.tato.repository.UserRepository;
import com.tato.repository.AttractionRepository;
import com.tato.service.AttractionService;
import com.tato.service.FavoriteService;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AttractionController {

    private final AttractionService attractionService;
    private final ReviewService reviewService;
    private final UserRepository userRepository;
    private final AttractionRepository attractionRepository;
    private final UserService userService;
    private final FavoriteService favoriteService;

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

    @GetMapping("/api/attractions")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getAllAttractions(Principal principal) {
        try {
            List<Attraction> attractions = attractionRepository.findAll();

            User currentUser = null;
            if (principal != null) {
                currentUser = userService.findByEmail(principal.getName());
            }

            List<Map<String, Object>> result = new ArrayList<>();

            for (Attraction attraction : attractions) {
                Map<String, Object> attractionData = new HashMap<>();
                attractionData.put("id", attraction.getId());
                attractionData.put("name", attraction.getName());
                attractionData.put("category", attraction.getCategory());
                attractionData.put("address", attraction.getAddress());
                attractionData.put("latitude", attraction.getLatitude());
                attractionData.put("longitude", attraction.getLongitude());
                attractionData.put("description", attraction.getDescription());

                // 찜하기 상태 추가
                boolean isFavorited = false;
                if (currentUser != null) {
                    isFavorited = favoriteService.isFavorited(currentUser, attraction);
                }
                attractionData.put("isFavorited", isFavorited);

                // 평균 평점 추가
                double avgRating = reviewService.getAverageRating(attraction.getId());
                attractionData.put("averageRating", avgRating);

                long reviewCount = reviewService.getReviewCount(attraction.getId());
                attractionData.put("reviewCount", reviewCount);

                result.add(attractionData);
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("관광지 데이터 조회 중 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}