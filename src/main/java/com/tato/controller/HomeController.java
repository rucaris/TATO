package com.tato.controller;

import com.tato.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class HomeController {

  private final UserService userService;

  @GetMapping("/")
  public String home(Model model, Principal principal) {
    if (principal != null) {
      var user = userService.findByEmail(principal.getName()); // 이메일로 조회
      model.addAttribute("username", user.getNickname());      // ✅ nickname 사용
      model.addAttribute("userEmail", user.getEmail());
    }
    return "index"; // templates/index.html
  }

  @GetMapping("/attractions")
  public String attractions() {
    return "attractions"; // templates/attractions.html
  }

  @GetMapping("/favorites")
  public String favorites(Model model, Principal principal) {
    if (principal != null) {
      var user = userService.findByEmail(principal.getName());
      model.addAttribute("username", user.getNickname());
    }
    return "favorites"; // templates/favorites.html
  }
}
