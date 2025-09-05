package com.tato.service;

import com.tato.model.Attraction;
import com.tato.model.Favorite;
import com.tato.model.User;
import com.tato.repository.AttractionRepository;
import com.tato.repository.FavoriteRepository;
import com.tato.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteService {

  private final FavoriteRepository favoriteRepository;
  private final UserRepository userRepository;
  private final AttractionRepository attractionRepository;

  @Transactional
  public boolean toggle(Long attractionId) {
    try {
      // 1. 인증 정보 확인
      String email = SecurityContextHolder.getContext().getAuthentication().getName();
      log.debug("현재 사용자 이메일: {}", email);

      if ("anonymousUser".equals(email)) {
        log.warn("익명 사용자가 즐겨찾기를 시도했습니다.");
        throw new RuntimeException("로그인이 필요합니다.");
      }

      // 2. 사용자 조회
      User user = userRepository.findByEmail(email)
              .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + email));

      log.debug("사용자 ID: {}, 닉네임: {}", user.getId(), user.getNickname());

      // 3. 관광지 조회 (CSV의 spot_id와 DB의 id 매핑 고려)
      Attraction attr = attractionRepository.findById(attractionId)
              .orElseThrow(() -> new RuntimeException("관광지를 찾을 수 없습니다. ID: " + attractionId));

      log.debug("관광지 ID: {}, 이름: {}", attr.getId(), attr.getName());

      // 4. 즐겨찾기 토글
      var existing = favoriteRepository.findByUserIdAndAttractionId(user.getId(), attr.getId());
      if (existing.isPresent()) {
        favoriteRepository.delete(existing.get());
        log.info("즐겨찾기 해제: 사용자={}, 관광지={}", user.getNickname(), attr.getName());
        return false; // 해제됨
      } else {
        Favorite f = new Favorite();
        f.setUser(user);
        f.setAttraction(attr);
        favoriteRepository.save(f);
        log.info("즐겨찾기 추가: 사용자={}, 관광지={}", user.getNickname(), attr.getName());
        return true;  // 찜됨
      }

    } catch (Exception e) {
      log.error("즐겨찾기 처리 중 오류 발생", e);
      throw new RuntimeException("즐겨찾기 처리 중 오류가 발생했습니다: " + e.getMessage());
    }
  }

  @Transactional(readOnly = true)
  public boolean isFavorite(Long attractionId) {
    try {
      String email = SecurityContextHolder.getContext().getAuthentication().getName();

      if ("anonymousUser".equals(email)) {
        return false;
      }

      User user = userRepository.findByEmail(email).orElse(null);
      if (user == null) {
        return false;
      }

      return favoriteRepository.findByUserIdAndAttractionId(user.getId(), attractionId).isPresent();
    } catch (Exception e) {
      log.error("즐겨찾기 상태 확인 중 오류", e);
      return false;
    }
  }
}