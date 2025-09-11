package com.tato.controller;

import com.tato.repository.FavoriteRepository;
import com.tato.service.FavoriteService;
import com.tato.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final UserService userService;
    private final FavoriteRepository favoriteRepository;

    @GetMapping("/favorites")
    public String favoritesPage(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            var user = userService.findByEmail(principal.getName());
            model.addAttribute("username", user.getNickname());
            model.addAttribute("userEmail", user.getEmail());
            var favorites = favoriteRepository.findAllByUserId(user.getId());
            model.addAttribute("favorites", favorites);

        } catch (Exception e) {
            log.error("즐겨찾기 페이지 로딩 중 오류", e);
            model.addAttribute("error", "즐겨찾기를 불러오는 중 오류가 발생했습니다.");
        }

        return "favorites";
    }

    // 관광지 신청 처리
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

    // 즐겨찾기 토글 API
    @PostMapping("/favorites/{attractionId}")
    @ResponseBody
    public Map<String, Object> toggleFavorite(@PathVariable Long attractionId, Principal principal) {
        Map<String, Object> response = new HashMap<>();

        if (principal == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return response;
        }

        try {
            // 실제 FavoriteService 호출
            boolean isFavorited = favoriteService.toggle(attractionId);
            response.put("success", true);
            response.put("favorited", isFavorited);
            response.put("message", isFavorited ?
                    "즐겨찾기에 추가되었습니다!" : "즐겨찾기에서 제거되었습니다.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "오류가 발생했습니다: " + e.getMessage());
            log.error("즐겨찾기 토글 중 오류", e);
        }

        return response;
    }
}