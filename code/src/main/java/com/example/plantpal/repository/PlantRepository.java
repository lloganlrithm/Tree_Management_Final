package com.example.plantpal.repository;

import com.example.plantpal.domain.entity.Plant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.List;
import java.util.Optional;

public interface PlantRepository extends JpaRepository<Plant, Long> {
    List<Plant> findByUserId(Long userId);

    // ใช้เช็กก่อนลบพันธุ์ไม้ ว่ามีต้นไม้ใช้พันธุ์นี้อยู่หรือไม่
    boolean existsBySpeciesId(Long speciesId);

        // ต้นไม้ทั้งหมดของผู้ใช้คนนี้ เรียงใหม่สุดก่อน
    List<Plant> findByUserEmailOrderByCreatedAtDesc(String email);

    // ค้นหาจากชื่อเล่น (ไม่สนตัวพิมพ์เล็ก/ใหญ่)
    List<Plant> findByUserEmailAndNicknameContainingIgnoreCaseOrderByCreatedAtDesc(String email, String keyword);

    // หาต้นไม้ 1 ต้น แต่ต้องเป็นของผู้ใช้คนนี้เท่านั้น
    Optional<Plant> findByIdAndUserEmail(Long id, String email);
}