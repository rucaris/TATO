package com.tato.repository;

import com.tato.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
  Optional<Favorite> findByUserIdAndAttractionId(Long userId, Long attractionId);
  List<Favorite> findAllByUserId(Long userId);
}
