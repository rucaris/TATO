/* package com.tato.controller;

import com.tato.service.FavoriteService;
import com.tato.service.ImageService;
import com.tato.service.UserService;
import com.tato.repository.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final FavoriteRepository favoriteRepository;
    private final UserService userService;
    private final ImageService imageService; //  ImageService 주입

    @GetMapping
    public String favoritesList(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            var user = userService.findByEmail(principal.getName());

            // 실제 즐겨찾기 데이터 조회
            var favorites = favoriteRepository.findAllByUserId(user.getId());

            // 템플릿용 데이터 변환
            List<Map<String, Object>> favoriteList = favorites.stream()
                    .map(fav -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("attractionId", fav.getAttraction().getId());
                        item.put("name", fav.getAttraction().getName());
                        item.put("address", fav.getAttraction().getAddress() != null ?
                                fav.getAttraction().getAddress() : "주소 정보 없음");
                        item.put("category", fav.getAttraction().getCategory());

                        // ✅ ImageService 사용
                        Long attractionId = fav.getAttraction().getId();
                        String imageUrl = imageService.getImageUrl(attractionId);
                        item.put("imageUrl", imageUrl);

                        return item;
                    })
                    .collect(Collectors.toList());

            model.addAttribute("favorites", favoriteList);
            model.addAttribute("favoritesCount", favorites.size());
            model.addAttribute("username", user.getNickname());

        } catch (Exception e) {
            e.printStackTrace();
            // 에러시 빈 데이터
            model.addAttribute("favorites", new ArrayList<>());
            model.addAttribute("favoritesCount", 0);
            model.addAttribute("error", "즐겨찾기를 불러오는데 실패했습니다.");
        }

        return "favorites";
    }

    @PostMapping("/attractions/submit")
    public String submitAttraction(@RequestParam String name,
                                   @RequestParam String category,
                                   @RequestParam(required = false) String address,
                                   @RequestParam(required = false) String latitude,
                                   @RequestParam(required = false) String longitude,
                                   @RequestParam String description,
                                   Principal principal,
                                   RedirectAttributes ra,
                                   Model model) {

        // 로그인 체크
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            // 사용자 정보
            var user = userService.findByEmail(principal.getName()).orElse(null);
            if (user != null) {
                model.addAttribute("username", user.getNickname());
            }

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

            // TODO: 실제로는 신청 테이블에 저장하거나 관리자 승인 대기 상태로 저장
            // 지금은 임시로 로그만 출력
            log.info("관광지 신청 접수: 사용자={}, 이름={}, 카테고리={}, 주소={}, 위도={}, 경도={}, 설명={}",
                    user != null ? user.getNickname() : "Unknown",
                    name, category, address, lat, lng, description);

            ra.addFlashAttribute("submitSuccess", "관광지 신청이 접수되었습니다! 검토 후 등록됩니다.");
            return "redirect:/favorites#submit";

        } catch (Exception e) {
            log.error("관광지 신청 처리 중 오류", e);
            ra.addFlashAttribute("submitError", "신청 처리 중 오류가 발생했습니다. 다시 시도해주세요.");
            return "redirect:/favorites#submit";
        }
    }

    @PostMapping("/{attractionId}")
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
            e.printStackTrace();
        }

        return response;
    }
} */