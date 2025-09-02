package com.tato.controller;

import com.tato.model.dto.RegisterForm;
import com.tato.exception.DuplicateEmailException;
import com.tato.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage() { return "login"; }

    @GetMapping("/register")
    public String registerForm(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new RegisterForm());
        }
        return "register"; // ✅ templates/register.html
    }

    @PostMapping("/register")
    public String registerSubmit(@Valid @ModelAttribute("form") RegisterForm form,
                                 BindingResult bindingResult,
                                 RedirectAttributes ra) {
        if (bindingResult.hasErrors()) return "register";
        try {
            userService.register(form.getEmail(), form.getPassword(), form.getNickname());
        } catch (DuplicateEmailException e) {
            bindingResult.rejectValue("email", "duplicate", "이미 사용 중인 이메일입니다.");
            return "register";
        }
        ra.addFlashAttribute("justRegisteredEmail", form.getEmail());
        return "redirect:/login?registered=true";
    }
}
