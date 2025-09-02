
package com.tato.controller;
import com.tato.service.AttractionService;
import com.tato.service.ReviewService;
import com.tato.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
@Controller @RequiredArgsConstructor
public class AttractionController {
  private final AttractionService attractionService;
  private final ReviewService reviewService;
  private final FavoriteService favoriteService;

  @GetMapping("/attractions/{id}")
  public String detail(@PathVariable Long id, Model model, Principal principal){
    var a = attractionService.findById(id).orElse(null);
    model.addAttribute("attraction", a);
    model.addAttribute("reviews", reviewService.list(id));
    model.addAttribute("isFav", principal!=null ?
        favoriteService.myList(principal.getName()).stream().anyMatch(f->f.getAttractionId().equals(id)) : false);
    if(principal!=null){ model.addAttribute("nickname", "김우성"); }
    return "attraction-detail";
  }

  @PostMapping("/attractions/{id}/reviews")
  public String addReview(@PathVariable Long id,
                          @RequestParam int rating,
                          @RequestParam String content,
                          Principal principal){
    String author = principal!=null ? "김우성" : "익명";
    reviewService.add(id, author, rating, content);
    return "redirect:/attractions/" + id;
  }

  @PostMapping("/attractions/{id}/favorite")
  public String toggle(@PathVariable Long id, Principal principal){
    if(principal!=null){ favoriteService.toggle(principal.getName(), id); }
    return "redirect:/attractions/" + id;
  }
}
