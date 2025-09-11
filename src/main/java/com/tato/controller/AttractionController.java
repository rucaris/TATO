/* package com.tato.controller;

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

  // 기존 상세 페이지
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

  // 기존 리뷰 추가
  @PostMapping("/attractions/{spotId}/reviews")
  public String addReview(@PathVariable String spotId,
                          @RequestParam int rating,
                          @RequestParam String content,
                          Principal principal,
                          RedirectAttributes ra) {
    if (principal == null) return "redirect:/login";

    var attraction = attractionService.findByIdOrSpotId(spotId);
    if (attraction == null) {
      ra.addFlashAttribute("error","관광지를 찾을 수 없습니다.");
      return "redirect:/attractions";
    }

    try {
      reviewService.addReview(attraction.getId(), content, rating);
      ra.addFlashAttribute("success","리뷰가 등록되었습니다!");
    } catch (Exception e) {
      log.error("리뷰 등록 중 오류", e);
      ra.addFlashAttribute("error", "이미 리뷰를 작성했습니다.");
    }

    return "redirect:/attractions/" + spotId;
  }

  // 🆕 API 엔드포인트 - 모든 관광지 조회 (index.html에서 사용)
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

  // 🆕 관광지 신청 처리 (favorites.html의 관광지 신청 탭에서 사용)
  @PostMapping("/attractions/submit")
  public String submitAttraction(@RequestParam String name,
                                 @RequestParam String category,
                                 @RequestParam(required = false) String address,
                                 @RequestParam(required = false) String latitude,
                                 @RequestParam(required = false) String longitude,
                                 @RequestParam String description,
                                 Principal principal,
                                 RedirectAttributes ra) {

    // 로그인 체크
    if (principal == null) {
      return "redirect:/login";
    }

    try {
      // 사용자 정보 조회
      var user = userService.findByEmail(principal.getName());
      log.info("관광지 신청 시도: 사용자={}", user.getNickname());

      // 입력값 검증
      if (name == null || name.trim().isEmpty()) {
        ra.addFlashAttribute("submitError", "관광지명은 필수 입력 항목입니다.");
        return "redirect:/favorites#submit";
      }

      if (category == null || category.trim().isEmpty()) {
        ra.addFlashAttribute("submitError", "카테고리는 필수 선택 항목입니다.");
        return "redirect:/favorites#submit";
      }

      if (description == null || description.trim().length() < 10) {
        ra.addFlashAttribute("submitError", "설명은 10자 이상 입력해주세요.");
        return "redirect:/favorites#submit";
      }

      // 좌표 검증 (선택사항)
      Double lat = null, lng = null;
      if (latitude != null && !latitude.trim().isEmpty()) {
        try {
          lat = Double.parseDouble(latitude.trim());
          if (lat < -90 || lat > 90) {
            ra.addFlashAttribute("submitError", "위도는 -90에서 90 사이의 값이어야 합니다.");
            return "redirect:/favorites#submit";
          }
        } catch (NumberFormatException e) {
          ra.addFlashAttribute("submitError", "올바른 위도 형식을 입력해주세요.");
          return "redirect:/favorites#submit";
        }
      }

      if (longitude != null && !longitude.trim().isEmpty()) {
        try {
          lng = Double.parseDouble(longitude.trim());
          if (lng < -180 || lng > 180) {
            ra.addFlashAttribute("submitError", "경도는 -180에서 180 사이의 값이어야 합니다.");
            return "redirect:/favorites#submit";
          }
        } catch (NumberFormatException e) {
          ra.addFlashAttribute("submitError", "올바른 경도 형식을 입력해주세요.");
          return "redirect:/favorites#submit";
        }
      }

      // 실제 관광지 신청 로직
      // TODO: 나중에 AttractionSubmission 엔티티를 만들어서 관리자 승인 대기 상태로 저장
      // 지금은 임시로 로그만 출력
      log.info("관광지 신청 접수 완료:");
      log.info("  - 신청자: {}", user.getNickname());
      log.info("  - 관광지명: {}", name.trim());
      log.info("  - 카테고리: {}", category.trim());
      log.info("  - 주소: {}", address != null ? address.trim() : "미입력");
      log.info("  - 좌표: {} / {}", lat != null ? lat : "미입력", lng != null ? lng : "미입력");
      log.info("  - 설명: {}", description.trim());

      ra.addFlashAttribute("submitSuccess",
              "관광지 신청이 접수되었습니다! 관리자 검토 후 등록됩니다. 감사합니다!");
      return "redirect:/favorites#submit";

    } catch (Exception e) {
      log.error("관광지 신청 처리 중 오류", e);
      ra.addFlashAttribute("submitError",
              "신청 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
      return "redirect:/favorites#submit";
    }
  }
} */