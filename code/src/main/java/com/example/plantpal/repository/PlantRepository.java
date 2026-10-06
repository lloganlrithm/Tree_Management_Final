package com.example.plantpal.repository;

import com.example.plantpal.domain.entity.Plant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlantRepository extends JpaRepository<Plant, Long> {
    List<Plant> findByUserId(Long userId);

    // ใช้เช็กก่อนลบพันธุ์ไม้ ว่ามีต้นไม้ใช้พันธุ์นี้อยู่หรือไม่
    boolean existsBySpeciesId(Long speciesId);
}