package com.tato.controller;

import com.tato.model.Attraction;
import com.tato.model.AttractionProposal;
import com.tato.model.User;
import com.tato.repository.AttractionRepository;
import com.tato.repository.FavoriteRepository;
import com.tato.service.AttractionProposalService;
import com.tato.service.FavoriteService;
import com.tato.service.UserService;
import com.tato.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final UserService userService;
    private final FavoriteRepository favoriteRepository;
    private final ImageService imageService;
    private final AttractionProposalService attractionProposalService;
    private final AttractionRepository attractionRepository;

    @GetMapping("/favorites")
    public String favoritesPage(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            var user = userService.findByEmail(principal.getName());
            model.addAttribute("username", user.getNickname());
            model.addAttribute("userEmail", user.getEmail());

            // 즐겨찾기 데이터를 템플릿에 맞게 변환
            var favorites = favoriteRepository.findAllByUserId(user.getId());

            List<Map<String, Object>> favoriteList = favorites.stream()
                    .map(fav -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("attractionId", fav.getAttraction().getId());
                        item.put("name", fav.getAttraction().getName());
                        item.put("address", fav.getAttraction().getAddress() != null ?
                                fav.getAttraction().getAddress() : "주소 정보 없음");
                        item.put("category", fav.getAttraction().getCategory());

                        // ImageService 사용해서 이미지 URL 추가
                        String imageUrl = imageService.getImageUrl(fav.getAttraction().getId());
                        item.put("imageUrl", imageUrl);

                        return item;
                    })
                    .collect(Collectors.toList());

            model.addAttribute("favorites", favoriteList);
            model.addAttribute("favoritesCount", favorites.size());

        } catch (Exception e) {
            log.error("즐겨찾기 페이지 로딩 중 오류", e);
            model.addAttribute("error", "즐겨찾기를 불러오는 중 오류가 발생했습니다.");
            model.addAttribute("favorites", List.of()); // 빈 리스트로 설정
            model.addAttribute("favoritesCount", 0);
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

            if (description == null || description.trim().length() < 5) {
                ra.addFlashAttribute("submitError", "설명은 5자 이상 입력해주세요.");
                return "redirect:/favorites#submit";
            }

            // 좌표 검증
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

            AttractionProposal proposal = AttractionProposal.builder()
                    .name(name.trim())
                    .category(category.trim())
                    .address(address != null ? address.trim() : null)
                    .latitude(lat)
                    .longitude(lng)
                    .description(description.trim())
                    .userId(user.getId())
                    .build();

            AttractionProposal savedProposal = attractionProposalService.submitProposal(proposal);

            log.info("관광지 신청이 성공적으로 저장되었습니다. ID: {}", savedProposal.getId());

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

    @GetMapping("/api/favorites/status/{spotId}")
    @ResponseBody
    public ResponseEntity<?> checkFavoriteStatus(@PathVariable String spotId,
                                                 Principal principal) {
        if (principal == null) {
            if ("all".equals(spotId)) {
                return ResponseEntity.ok(List.of());
            }
            Map<String, Object> response = new HashMap<>();
            response.put("favorited", false);
            response.put("success", true);
            return ResponseEntity.ok(response);
        }

        try {
            User user = userService.findByEmail(principal.getName());

            if ("all".equals(spotId)) {
                // all 처리
                List<Map<String, Long>> userFavorites = favoriteRepository.findAllByUserId(user.getId())
                        .stream()
                        .map(fav -> Map.of("attractionId", fav.getAttraction().getId()))
                        .collect(Collectors.toList());
                return ResponseEntity.ok(userFavorites);
            } else {
                // 단일 처리
                Map<String, Object> response = new HashMap<>();
                Long attractionId = Long.parseLong(spotId);
                Attraction attraction = attractionRepository.findById(attractionId)
                        .orElseThrow(() -> new RuntimeException("관광지를 찾을 수 없습니다: " + spotId));

                boolean isFavorited = favoriteService.isFavorited(user, attraction);
                response.put("favorited", isFavorited);
                response.put("success", true);
                return ResponseEntity.ok(response);
            }
        } catch (NumberFormatException e) {
            log.error("잘못된 spotId 형식: {}", spotId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "올바르지 않은 관광지 ID입니다.");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("찜하기 상태 확인 중 오류", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "상태 확인에 실패했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/favorites/{spotId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleFavoriteDetail(@PathVariable String spotId,
                                                                    Principal principal) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (principal == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            User user = userService.findByEmail(principal.getName());

            // spotId를 Long으로 변환
            Long attractionId = Long.parseLong(spotId);
            Attraction attraction = attractionRepository.findById(attractionId)
                    .orElseThrow(() -> new RuntimeException("관광지를 찾을 수 없습니다: " + spotId));

            boolean wasAlreadyFavorited = favoriteService.isFavorited(user, attraction);

            if (wasAlreadyFavorited) {
                favoriteService.removeFavorite(user, attraction);
                response.put("favorited", false);
                response.put("message", "찜 목록에서 해제되었습니다");
            } else {
                favoriteService.addFavorite(user, attraction);
                response.put("favorited", true);
                response.put("message", "찜 목록에 추가되었습니다");
            }

            response.put("success", true);

            return ResponseEntity.ok(response);

        } catch (NumberFormatException e) {
            log.error("잘못된 spotId 형식: {}", spotId, e);
            response.put("success", false);
            response.put("message", "올바르지 않은 관광지 ID입니다.");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("찜하기 토글 중 오류", e);
            response.put("success", false);
            response.put("message", "찜하기 처리에 실패했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/api/favorites/toggle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleFavoriteApi(@RequestBody Map<String, Long> payload, Principal principal) {
        Map<String, Object> response = new HashMap<>();
        Long attractionId = payload.get("attractionId");

        if (principal == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        if (attractionId == null) {
            response.put("success", false);
            response.put("message", "관광지 ID가 필요합니다.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            User user = userService.findByEmail(principal.getName());
            Attraction attraction = attractionRepository.findById(attractionId)
                    .orElseThrow(() -> new RuntimeException("관광지를 찾을 수 없습니다: " + attractionId));

            boolean wasFavorited = favoriteService.isFavorited(user, attraction);

            if (wasFavorited) {
                favoriteService.removeFavorite(user, attraction);
                response.put("favorited", false);
            } else {
                favoriteService.addFavorite(user, attraction);
                response.put("favorited", true);
            }

            response.put("success", true);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("찜하기 API 처리 중 오류", e);
            response.put("success", false);
            response.put("message", "찜하기 처리에 실패했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /* 코드 충돌로 잠시 주석처리
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

            log.debug("즐겨찾기 토글 완료: attractionId={}, isFavorited={}", attractionId, isFavorited);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "오류가 발생했습니다: " + e.getMessage());
            log.error("즐겨찾기 토글 중 오류: attractionId={}", attractionId, e);
        }

        return response;
    } */
}