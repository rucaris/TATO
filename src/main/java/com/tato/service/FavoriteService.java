package com.tato.service;

import com.tato.model.Attraction;
import com.tato.model.Favorite;
import com.tato.model.User;
import com.tato.repository.AttractionRepository;
import com.tato.repository.FavoriteRepository;
import com.tato.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {

  private final FavoriteRepository favoriteRepository;
  private final UserRepository userRepository;
  private final AttractionRepository attractionRepository;

  @Transactional
  public boolean toggle(Long attractionId) {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userRepository.findByEmail(email).orElseThrow();
    Attraction attr = attractionRepository.findById(attractionId).orElseThrow();

    var existing = favoriteRepository.findByUserIdAndAttractionId(user.getId(), attr.getId());
    if (existing.isPresent()) {
      favoriteRepository.delete(existing.get());
      return false; // 해제됨
    } else {
      Favorite f = new Favorite();
      f.setUser(user);
      f.setAttraction(attr);
      favoriteRepository.save(f);
      return true;  // 찜됨
    }
  }

  @Transactional(readOnly = true)
  public boolean isFavorite(Long attractionId) {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userRepository.findByEmail(email).orElseThrow();
    return favoriteRepository.findByUserIdAndAttractionId(user.getId(), attractionId).isPresent();
  }

  @Transactional(readOnly = true)
  public List<Favorite> getUserFavorites() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userRepository.findByEmail(email).orElseThrow();
    return favoriteRepository.findAllByUserId(user.getId());
  }
}