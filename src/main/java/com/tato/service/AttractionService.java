package com.tato.service;

import com.tato.model.Attraction;
import com.tato.repository.AttractionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttractionService {
  private final AttractionRepository repo;

  public Optional<Attraction> findById(Long id) {
    return repo.findById(id);
  }

  /**
   * ID 또는 spotId로 관광지 조회
   * 먼저 숫자인지 확인하고 DB ID로 조회, 없으면 spotId로 조회
   */
  public Attraction findByIdOrSpotId(String idOrSpotId) {
    log.debug("관광지 조회 시도: {}", idOrSpotId);

    try {
      // 1. 먼저 숫자인지 확인하고 DB ID로 조회
      Long id = Long.parseLong(idOrSpotId);
      Optional<Attraction> byId = repo.findById(id);
      if (byId.isPresent()) {
        log.debug("DB ID로 조회 성공: {}", id);
        return byId.get();
      }
    } catch (NumberFormatException e) {
      log.debug("숫자가 아님, spotId로 조회: {}", idOrSpotId);
    }

    // 2. spotId로 조회
    Optional<Attraction> bySpotId = repo.findBySpotId(idOrSpotId);
    if (bySpotId.isPresent()) {
      log.debug("spotId로 조회 성공: {}", idOrSpotId);
      return bySpotId.get();
    }

    log.warn("관광지 조회 실패: {}", idOrSpotId);
    return null;
  }

  public List<Attraction> findAllAttractions() {
    return repo.findAll();
  }

  public Attraction findAttractionById(Long id) {
    return repo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("해당 번호의 관광지가 없습니다. " + id));
  }

  public void updateAttraction(Long id, Attraction updatedAttraction) {
    Attraction existingAttraction = repo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("해당 번호의 관광지가 없습니다. " + id));

    existingAttraction.setName(updatedAttraction.getName());
    existingAttraction.setCategory(updatedAttraction.getCategory());
    existingAttraction.setAddress(updatedAttraction.getAddress());
    existingAttraction.setDescription(updatedAttraction.getDescription());

    repo.save(existingAttraction);
  }

  public void deleteAttraction(Long id) {
    if (!repo.existsById(id)) {
      throw new IllegalArgumentException("해당 번호의 관광지가 없습니다. " + id);
    }
    repo.deleteById(id);
  }
}