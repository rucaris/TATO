package com.tato.controller;

import com.tato.model.dto.UserRegisterDto;
import com.tato.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.tato.exception.DuplicateEmailException;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) model.addAttribute("error", "이메일 또는 비밀번호가 올바르지 않습니다.");
        if (logout != null) model.addAttribute("logout", "로그아웃되었습니다.");
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new UserRegisterDto()); // 원래 클래스명 사용
        }
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("form") UserRegisterDto form,
            BindingResult binding,
            RedirectAttributes redirect) {

        System.out.println("=== 회원가입 시도 ===");
        System.out.println("이메일: " + form.getEmail());
        System.out.println("이름: " + form.getName());
        System.out.println("검증 오류: " + binding.hasErrors());

        if (binding.hasErrors()) {
            System.out.println("검증 실패!");
            binding.getAllErrors().forEach(error ->
                    System.out.println("오류: " + error.getDefaultMessage()));

            // 검증 실패 → 폼과 에러를 그대로 다시 보여줌
            redirect.addFlashAttribute("org.springframework.validation.BindingResult.form", binding);
            redirect.addFlashAttribute("form", form);
            return "redirect:/register";
        }

        try {
            System.out.println("UserService.register 호출 시작");
            userService.register(form.getEmail(), form.getPassword(), form.getName());
            System.out.println("회원가입 성공!");
        } catch (DuplicateEmailException e) {
            System.out.println("중복 이메일 오류: " + e.getMessage());
            binding.rejectValue("email", "duplicate", "이미 가입된 이메일입니다.");
            redirect.addFlashAttribute("org.springframework.validation.BindingResult.form", binding);
            redirect.addFlashAttribute("form", form);
            return "redirect:/register";
        } catch (Exception e) {
            System.out.println("예상치 못한 오류: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/register?error=true";
        }

        System.out.println("로그인 페이지로 리다이렉트");
        // 가입 성공 → 로그인 페이지로
        return "redirect:/login?registered=true";
    }
}