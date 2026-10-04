package com.example.plantpal.repository;

import com.example.plantpal.domain.entity.Species;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SpeciesRepository extends JpaRepository<Species, Long> {
    Optional<Species> findByName(String name);
}
