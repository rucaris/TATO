package com.tato.repository;

import com.tato.model.Attraction;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AttractionRepository extends JpaRepository<Attraction, Long>{}