package com.tato.controller;

import com.tato.service.FavoriteService;
import com.tato.service.UserService;
import com.tato.repository.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    private final FavoriteService favoriteService;
    private final FavoriteRepository favoriteRepository;
    private final UserService userService;

    // 즐겨찾기 목록 페이지
    @GetMapping
    public String favoritesList(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            var user = userService.findByEmail(principal.getName());

            // 사용자의 즐겨찾기 목록 조회
            var favorites = favoriteRepository.findAllByUserId(user.getId());

            // 템플릿에서 사용할 데이터 형태로 변환
            List<Map<String, Object>> favoriteList = favorites.stream()
                    .map(fav -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("attractionId", fav.getAttraction().getId());
                        item.put("name", fav.getAttraction().getName());
                        item.put("address", fav.getAttraction().getAddress() != null ?
                                fav.getAttraction().getAddress() : "주소 정보 없음");
                        item.put("category", fav.getAttraction().getCategory());
                        return item;
                    })
                    .collect(Collectors.toList());

            model.addAttribute("favorites", favoriteList);
            model.addAttribute("favoritesCount", favorites.size());
            model.addAttribute("username", user.getNickname());

        } catch (Exception e) {
            // 에러 발생 시 빈 목록으로 처리
            model.addAttribute("favorites", new ArrayList<>());
            model.addAttribute("favoritesCount", 0);
            model.addAttribute("error", "즐겨찾기 목록을 불러오는데 실패했습니다.");
        }

        return "favorites";
    }


    // 즐겨찾기 토글 (추가/제거)
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
            boolean isFavorited = favoriteService.toggle(attractionId);
            response.put("success", true);
            response.put("favorited", isFavorited);
            response.put("message", isFavorited ? "즐겨찾기에 추가되었습니다." : "즐겨찾기에서 제거되었습니다.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "오류가 발생했습니다: " + e.getMessage());
        }

        return response;
    }

    // 즐겨찾기 제거 (DELETE 방식)
    @PostMapping("/{attractionId}/remove")
    public String removeFavorite(@PathVariable Long attractionId,
                                 Principal principal,
                                 RedirectAttributes ra) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            favoriteService.toggle(attractionId); // toggle로 제거
            ra.addFlashAttribute("success", "즐겨찾기에서 제거되었습니다.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "제거 중 오류가 발생했습니다.");
        }

        return "redirect:/favorites";
    }

    // AJAX용 즐겨찾기 상태 확인
    @GetMapping("/{attractionId}/status")
    @ResponseBody
    public Map<String, Object> getFavoriteStatus(@PathVariable Long attractionId, Principal principal) {
        Map<String, Object> response = new HashMap<>();

        if (principal == null) {
            response.put("favorited", false);
            return response;
        }

        try {
            boolean isFavorited = favoriteService.isFavorite(attractionId);
            response.put("favorited", isFavorited);
        } catch (Exception e) {
            response.put("favorited", false);
        }

        return response;
    }
}