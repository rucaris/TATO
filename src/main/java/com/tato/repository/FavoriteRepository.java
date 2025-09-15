package com.tato.repository;

import com.tato.model.Attraction;
import com.tato.model.Favorite;
import com.tato.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
  Optional<Favorite> findByUserIdAndAttractionId(Long userId, Long attractionId);
  List<Favorite> findAllByUserId(Long userId);
  Optional<Favorite> findByUserAndAttraction(User user, Attraction attraction);
  boolean existsByUserAndAttraction(User user, Attraction attraction);
  void deleteByUserAndAttraction(User user, Attraction attraction);
}
