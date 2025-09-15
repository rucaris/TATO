package com.tato.controller;

import com.tato.model.User;
import com.tato.model.Attraction;
import com.tato.model.Review;
import com.tato.service.ReviewService;
import com.tato.service.ReviewService;
import com.tato.service.AttractionService;
import com.tato.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Map;
import java.util.HashMap;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/reviews")

public class ReviewController {

    private final ReviewService reviewService;
    private final AttractionService attractionService;
    private final UserService userService;

    @PostMapping("/attractions/{spotId}")
    public String addReview(@PathVariable String spotId,
                            @RequestParam int rating,
                            @RequestParam String content,
                            Principal principal,
                            RedirectAttributes ra) {
        if (principal == null) {
            ra.addFlashAttribute("error", "로그인이 필요한 기능입니다.");
            return "redirect:/login";
        }

        try {
            Long id = Long.parseLong(spotId);
            var attraction = attractionService.findById(id).orElse(null);

            if (attraction == null) {
                ra.addFlashAttribute("error", "관광지를 찾을 수 없습니다.");
                return "redirect:/attractions";
            }

            User currentUser = userService.findByEmail(principal.getName());

            if (reviewService.hasUserReviewedAttraction(currentUser, attraction)) {
                ra.addFlashAttribute("error", "이미 리뷰를 작성하셨습니다.");
                return "redirect:/attractions/" + spotId;
            }

            reviewService.addReview(attraction.getId(), content, rating, currentUser);
            ra.addFlashAttribute("success", "리뷰가 등록되었습니다.");

        } catch (NumberFormatException e) {
            log.error("잘못된 spotId 형식: {}", spotId, e);
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/attractions";
        } catch (Exception e) {
            log.error("리뷰 등록 중 오류", e);
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/attractions/" + spotId;
    }

    @PostMapping("/{reviewId}/edit")
    public String editReview(@PathVariable Long reviewId,
                             @RequestParam("content") String content,
                             @RequestParam("rating") int rating,
                             Principal principal,
                             RedirectAttributes ra) {

        try {
            if (principal == null) {
                ra.addFlashAttribute("error", "로그인이 필요합니다.");
                return "redirect:/login";
            }

            User currentUser = userService.findByEmail(principal.getName());
            Review review = reviewService.findById(reviewId);


            if (!review.getUser().getId().equals(currentUser.getId())) {
                ra.addFlashAttribute("error", "본인의 리뷰만 수정 가능");
                return "redirect:/attractions" + review.getAttraction().getSpotId();
            }

            if (content == null || content.trim().isEmpty()) {
                ra.addFlashAttribute("error", "리뷰 내용을 입력해주세요.");
                return "redirect:/attractions" + review.getAttraction().getSpotId();
            }

            if (rating < 1) {
                ra.addFlashAttribute("error", "별점은 1~5점 사이에서 골라주세요.");
                return "redirect:/attractions" + review.getAttraction().getSpotId();
            }

            reviewService.updateReview(reviewId, content, rating);
            ra.addFlashAttribute("success", "리뷰가 수정되었습니다.");

            return "redirect:/attractions" + review.getAttraction().getSpotId();
        } catch (Exception e) {
            log.error("리뷰 수정 중 오류", e);
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/attractions";
        }
    }

    //삭제 기능

    @PostMapping("/{reviewId}/delete")
    public String deleteReview(@PathVariable Long reviewId,
                               Principal principal,
                               RedirectAttributes ra) {
        try {
            if (principal == null) {
                ra.addFlashAttribute("error", "로그인이 필요합니다.");
                return "redirect:/login";
            }

            User currentUser = userService.findByEmail(principal.getName());
            Review review = reviewService.findById(reviewId);
            String spotId = review.getAttraction().getSpotId();

            //일반 / 관리자 유저인지 구분
            if (!review.getUser().getId().equals(currentUser.getId()) &&
                    !currentUser.getRole().equals("ADMIN")) {
                ra.addFlashAttribute("error", "삭제 권한이 없습니다.");
                return "redirect:/attractions/" + spotId;
            }

            reviewService.deleteReview(reviewId);

            String message = currentUser.getRole().equals("ADMIN") &&
                    !review.getUser().getId().equals(currentUser.getId()) ?
                    "리뷰가 삭제되었습니다 (관리자)" : "리뷰가 삭제되었스빈다.";

            ra.addFlashAttribute("success", message);

            return "redirect:/attractions/" + spotId;
        } catch (Exception e) {
            log.error("리뷰 삭제 오류", e);
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/attractions";
        }
    }

    @PostMapping("/api/{reviewId}/edit")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> editReviewApi(@PathVariable Long reviewId,
                                                             @RequestBody Map<String, Object> requestData,
                                                             Principal principal) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (principal == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            User currentUser = userService.findByEmail(principal.getName());
            Review review = reviewService.findById(reviewId);

            // 권한 체크
            if (!review.getUser().getId().equals(currentUser.getId())) {
                response.put("success", false);
                response.put("message", "수정 권한이 없습니다.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            String content = (String) requestData.get("content");
            Integer rating = (Integer) requestData.get("rating");

            // 입력 검증
            if (content == null || content.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "리뷰 내용을 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            if (rating == null || rating < 1) {
                response.put("success", false);
                response.put("message", "별점은 1~5점 사이로 선택해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            reviewService.updateReview(reviewId, content, rating);

            response.put("success", true);
            response.put("message", "리뷰가 수정되었습니다.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("API 리뷰 수정 중 오류", e);
            response.put("success", false);
            response.put("message", "리뷰 수정에 실패했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/api/{reviewId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteReviewApi(@PathVariable Long reviewId,
                                                               Principal principal) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (principal == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            User currentUser = userService.findByEmail(principal.getName());
            Review review = reviewService.findById(reviewId);

            // 본인 리뷰인지 또는 관리자인지 확인
            if (!review.getUser().getId().equals(currentUser.getId()) &&
                    !currentUser.getRole().equals("ADMIN")) {
                response.put("success", false);
                response.put("message", "삭제 권한이 없습니다.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            boolean isAdminDelete = currentUser.getRole().equals("ADMIN") &&
                    !review.getUser().getId().equals(currentUser.getId());

            reviewService.deleteReview(reviewId);

            String message = isAdminDelete ?
                    "리뷰가 삭제되었습니다! (관리자)" :
                    "리뷰가 삭제되었습니다!";

            response.put("success", true);
            response.put("message", message);
            response.put("isAdminDelete", isAdminDelete);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("API 리뷰 삭제 중 오류", e);
            response.put("success", false);
            response.put("message", "리뷰 삭제에 실패했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/api/user/{userId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUserReviews(@PathVariable Long userId,
                                                              Principal principal) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (principal == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            User currentUser = userService.findByEmail(principal.getName());

            // 본인 또는 관리자만 접근 가능
            if (!currentUser.getId().equals(userId) && !currentUser.getRole().equals("ADMIN")) {
                response.put("success", false);
                response.put("message", "접근 권한이 없습니다.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            var reviews = reviewService.findByUserId(userId);

            response.put("success", true);
            response.put("reviews", reviews);
            response.put("count", reviews.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("사용자 리뷰 조회 중 오류", e);
            response.put("success", false);
            response.put("message", "리뷰 조회에 실패했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    }
