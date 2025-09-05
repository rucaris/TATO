package com.tato.controller;

import com.tato.service.AttractionService;
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
    private final ImageService imageService;
    private final AttractionService attractionService;

    @GetMapping
    public String favoritesList(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            var user = userService.findByEmail(principal.getName());

            var favorites = favoriteRepository.findAllByUserId(user.getId());

            List<Map<String, Object>> favoriteList = favorites.stream()
                    .map(fav -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("attractionId", fav.getAttraction().getSpotId()); // spotId 사용
                        item.put("name", fav.getAttraction().getName());
                        item.put("address", fav.getAttraction().getAddress() != null ?
                                fav.getAttraction().getAddress() : "주소 정보 없음");
                        item.put("category", fav.getAttraction().getCategory());

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
            model.addAttribute("favorites", new ArrayList<>());
            model.addAttribute("favoritesCount", 0);
            model.addAttribute("error", "즐겨찾기를 불러오는데 실패했습니다.");
        }

        return "favorites";
    }

    @PostMapping("/{attractionIdOrSpotId}")
    @ResponseBody
    public Map<String, Object> toggleFavorite(@PathVariable String attractionIdOrSpotId, Principal principal) {
        Map<String, Object> response = new HashMap<>();

        if (principal == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return response;
        }

        try {
            log.debug("즐겨찾기 토글 요청: {}", attractionIdOrSpotId);

            // spotId 또는 id로 관광지 찾기
            var attraction = attractionService.findByIdOrSpotId(attractionIdOrSpotId);
            if (attraction == null) {
                response.put("success", false);
                response.put("message", "관광지를 찾을 수 없습니다.");
                return response;
            }

            // 실제 DB ID로 즐겨찾기 처리
            boolean isFavorited = favoriteService.toggle(attraction.getId());
            response.put("success", true);
            response.put("favorited", isFavorited);
            response.put("message", isFavorited ?
                    "즐겨찾기에 추가되었습니다!" : "즐겨찾기에서 제거되었습니다.");

        } catch (Exception e) {
            log.error("즐겨찾기 처리 중 오류: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "즐겨찾기 처리 중 오류가 발생했습니다: " + e.getMessage());
        }

        return response;
    }
}