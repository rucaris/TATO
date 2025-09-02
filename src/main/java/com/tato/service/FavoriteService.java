
package com.tato.service;
import com.tato.model.Favorite;
import com.tato.repository.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
@Service @RequiredArgsConstructor
public class FavoriteService {
  private final FavoriteRepository repo;
  public List<Favorite> myList(String username){ return repo.findByUsername(username); }
  public boolean toggle(String username, Long attractionId){
    var exist = repo.findByUsernameAndAttractionId(username, attractionId);
    if(exist.isPresent()){ repo.delete(exist.get()); return false; }
    repo.save(Favorite.builder().username(username).attractionId(attractionId).build());
    return true;
  }
}
