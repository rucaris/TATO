
package com.tato.service;
import com.tato.model.Attraction;
import com.tato.repository.AttractionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;
@Service @RequiredArgsConstructor
public class AttractionService {
  private final AttractionRepository repo;
  public Optional<Attraction> findById(Long id){ return repo.findById(id); }
  public List<Attraction> findAllAttractions() { return repo.findAll(); }

  public Attraction findAttractionById(Long id) {
    return repo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("해당 번호의 관광지가 없습니다. " + id));
  }

  public Attraction createAttraction(Attraction attraction) {
    try {
      return repo.save(attraction);
    } catch (DataIntegrityViolationException e) {
      throw new IllegalArgumentException("이미 동일한 위도와 경도를 가진 관광지가 존재합니다.", e);
    }
  }

  public void updateAttraction(Long id, Attraction updatedAttraction) {
    Attraction existingAttraction = repo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("해당 번호의 관광지가 없습니다. " + id));

    existingAttraction.setName(updatedAttraction.getName());
    existingAttraction.setCategory(updatedAttraction.getCategory());
    existingAttraction.setAddress(updatedAttraction.getAddress());
    existingAttraction.setDescription(updatedAttraction.getDescription());
    existingAttraction.setLatitude(updatedAttraction.getLatitude());
    existingAttraction.setLongitude(updatedAttraction.getLongitude());

    try {
      repo.save(existingAttraction);
    } catch (DataIntegrityViolationException e) {
      throw new IllegalArgumentException("이미 동일한 위도와 경도를 가진 관광지가 존재합니다.", e);
    }
  }

  public void deleteAttraction(Long id) {
    if (!repo.existsById(id)) {
      throw new IllegalArgumentException("해당 번호의 관광지가 없습니다. " + id);
    }
    repo.deleteById(id);
  }
}