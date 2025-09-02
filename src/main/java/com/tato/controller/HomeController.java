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
      try {
        var user = userService.findByEmail(principal.getName());
        model.addAttribute("username", user.getName());
        model.addAttribute("userEmail", principal.getName());
      } catch (Exception e) {
        model.addAttribute("username", principal.getName()); // 실패시 이메일 사용
      }
    }
    return "index"; // templates/index.html로 이동됨
  }

  @GetMapping("/attractions")
  public String attractions() {
    return "attractions"; // templates/attractions.html로 이동이요
  }

  @GetMapping("/favorites")
  public String favorites(Model model, Principal principal) {
    if (principal != null) {
      try {
        var user = userService.findByEmail(principal.getName());
        model.addAttribute("username", user.getName());
      } catch (Exception e) {
        model.addAttribute("username", principal.getName());
      }
    }
    return "favorites"; // templates/favorites.html로 이동
  }
}