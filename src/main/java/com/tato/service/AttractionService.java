
package com.tato.service;
import com.tato.model.Attraction;
import com.tato.repository.AttractionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;
@Service @RequiredArgsConstructor
public class AttractionService {
  private final AttractionRepository repo;
  public Optional<Attraction> findById(Long id){ return repo.findById(id); }
}
