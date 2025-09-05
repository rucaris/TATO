package com.tato.controller;

import com.tato.model.User;
import com.tato.service.AttractionService;
import com.tato.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final AttractionService attractionService;

    @GetMapping("/admin")
    public String adminPage() {
        return "admin";
    }

    @GetMapping("/admin/users")
    public String adminUsersPage(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        return "admin/users";
    }

    @GetMapping("/admin/users/{id}")
    @ResponseBody
    public ResponseEntity<User> getUserDetails(@PathVariable Long id) {
        try {
            User user = userService.findUserById(id);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/admin/users/{id}/role")
    @ResponseBody
    public ResponseEntity<Void> updateUserRole(@PathVariable Long id, @RequestParam String role) {
        try {
            userService.updateUserRole(id, role);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/admin/users/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/admin/attractions")
    public String adminAttractionsPage(Model model) {
        model.addAttribute("attractions", attractionService.findAllAttractions());
        return "admin/attractions";
    }

    @GetMapping("/api/admin/attractions/{id}")
    @ResponseBody
    public ResponseEntity<com.tato.model.Attraction> getAttractionDetails(@PathVariable Long id) {
        try {
            com.tato.model.Attraction attraction = attractionService.findAttractionById(id);
            return ResponseEntity.ok(attraction);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/api/admin/attractions/{id}")
    @ResponseBody
    public ResponseEntity<Void> updateAttraction(@PathVariable Long id, @RequestBody com.tato.model.Attraction attraction) {
        try {
            attractionService.updateAttraction(id, attraction);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/api/admin/attractions/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteAttraction(@PathVariable Long id) {
        try {
            attractionService.deleteAttraction(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}