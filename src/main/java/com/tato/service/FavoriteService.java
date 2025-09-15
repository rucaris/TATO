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
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
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
  public boolean isFavorited(User user, Attraction attraction) {
    return favoriteRepository.existsByUserAndAttraction(user, attraction);
  }

  @Transactional
  public void addFavorite(User user, Attraction attraction) {
    // 이미 찜하기가 되어있는지 확인 (중복 방지)
    if (!favoriteRepository.existsByUserAndAttraction(user, attraction)) {
      Favorite favorite = new Favorite();
      favorite.setUser(user);
      favorite.setAttraction(attraction);
      favoriteRepository.save(favorite);

      log.info("찜하기 추가: 사용자={}, 관광지={}", user.getNickname(), attraction.getName());
    } else {
      log.warn("이미 찜하기된 관광지: 사용자={}, 관광지={}", user.getNickname(), attraction.getName());
    }
  }

  @Transactional
  public void removeFavorite(User user, Attraction attraction) {
    Optional<Favorite> favorite = favoriteRepository.findByUserAndAttraction(user, attraction);

    if (favorite.isPresent()) {
      favoriteRepository.delete(favorite.get());
      log.info("찜하기 해제: 사용자={}, 관광지={}", user.getNickname(), attraction.getName());
    } else {
      log.warn("찜하기되지 않은 관광지 해제 시도: 사용자={}, 관광지={}", user.getNickname(), attraction.getName());
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