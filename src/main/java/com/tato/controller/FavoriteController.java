package com.tato.controller;

import com.tato.service.FavoriteService;
import com.tato.service.UserService;
import com.tato.repository.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/favorites")
public class FavoriteController {

    // 실제 서비스들 주입
    private final FavoriteService favoriteService;
    private final FavoriteRepository favoriteRepository;
    private final UserService userService;

    // 즐겨찾기 목록 페이지 (실제 데이터 조회)
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

                        Long attractionId = fav.getAttraction().getId();
                        String imageUrl = getImageUrl(attractionId);
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

    // 즐겨찾기 토글 (실제 서비스 호출)
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
    private String getImageUrl(Long attractionId) {
        Map<Long, String> imageUrls = Map.of(
                1L, "/images/attractions/architecture_img01.jpg",
                2L, "/images/attractions/architecture_img02.jpg",
                3L, "/images/attractions/architecture_img03.jpg",
                4L, "/images/attractions/architecture_img04.jpg",
                5L, "/images/attractions/architecture_img05.jpg"
        );
        return imageUrls.getOrDefault(attractionId,
                "https://images.unsplash.com/photo-1513475382585-d06e58bcb0e0?w=300&h=200&fit=crop");
    }
}