package com.tato.repository;

import com.tato.model.Attraction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AttractionRepository extends JpaRepository<Attraction, Long> {

    // spotId로 관광지 조회 (CSV의 spot_id와 매핑)
    Optional<Attraction> findBySpotId(String spotId);

    // spotId 존재 여부 확인
    boolean existsBySpotId(String spotId);
}