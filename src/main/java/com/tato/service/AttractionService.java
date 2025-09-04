
package com.tato.service;
import com.tato.model.Attraction;
import com.tato.repository.AttractionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
