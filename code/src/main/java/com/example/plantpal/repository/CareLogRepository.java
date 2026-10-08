package com.example.plantpal.repository;

import com.example.plantpal.domain.entity.CareLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CareLogRepository extends JpaRepository<CareLog, Long> {
    List<CareLog> findByPlantIdOrderByPerformedAtDesc(Long plantId);

    // ===== ประวัติการดูแล (C3) =====
    // ดึง plant + species มาพร้อมกัน เพราะปิด open-in-view ไว้
    // หน้าเว็บจะได้แสดงชื่อต้นไม้ได้

    // ประวัติของต้นไม้ทุกต้นของผู้ใช้คนนี้ ใหม่สุดก่อน
    @Query("""
            select cl from CareLog cl
            join fetch cl.plant p
            join fetch p.species
            where p.user.id = :userId
            order by cl.performedAt desc, cl.id desc
            """)
    List<CareLog> findHistoryByOwner(@Param("userId") Long userId);

    // ประวัติของต้นไม้ 1 ต้น (ต้องเป็นของผู้ใช้คนนี้เท่านั้น) ใหม่สุดก่อน
    @Query("""
            select cl from CareLog cl
            join fetch cl.plant p
            join fetch p.species
            where p.id = :plantId and p.user.id = :userId
            order by cl.performedAt desc, cl.id desc
            """)
    List<CareLog> findHistoryByPlantAndOwner(@Param("plantId") Long plantId, @Param("userId") Long userId);
}