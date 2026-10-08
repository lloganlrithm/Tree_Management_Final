package com.example.plantpal.repository;

import com.example.plantpal.domain.entity.CareSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CareScheduleRepository extends JpaRepository<CareSchedule, Long> {
    List<CareSchedule> findByPlantId(Long plantId);

    // ใช้ตอน scheduler หา "งานที่ถึงกำหนด"
    List<CareSchedule> findByIsActiveTrueAndNextDueDateLessThanEqual(LocalDate date);

    // ===== หน้า care (C1) =====
    // ทุก query ดึง plant + species มาพร้อมกัน (join fetch) เพราะปิด open-in-view
    // ไว้
    // ถ้าไม่ fetch มาก่อน หน้า Thymeleaf จะเรียก schedule.plant.nickname ไม่ได้
    // (LazyInitializationException)

    // เลยกำหนด: nextDueDate < date
    @Query("""
            select cs from CareSchedule cs
            join fetch cs.plant p
            join fetch p.species
            where p.user.id = :userId and cs.isActive = true
              and cs.nextDueDate < :date
            order by cs.nextDueDate asc, cs.id asc
            """)
    List<CareSchedule> findActiveDueBefore(@Param("userId") Long userId, @Param("date") LocalDate date);

    // ช่วงวันที่ from..to (รวมทั้งสองวัน) ใช้ทั้ง "วันนี้" (from = to = วันนี้) และ
    // dashboard
    @Query("""
            select cs from CareSchedule cs
            join fetch cs.plant p
            join fetch p.species
            where p.user.id = :userId and cs.isActive = true
              and cs.nextDueDate between :from and :to
            order by cs.nextDueDate asc, cs.id asc
            """)
    List<CareSchedule> findActiveDueBetween(@Param("userId") Long userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    // ถัดไป: nextDueDate > date
    @Query("""
            select cs from CareSchedule cs
            join fetch cs.plant p
            join fetch p.species
            where p.user.id = :userId and cs.isActive = true
              and cs.nextDueDate > :date
            order by cs.nextDueDate asc, cs.id asc
            """)
    List<CareSchedule> findActiveDueAfter(@Param("userId") Long userId, @Param("date") LocalDate date);

    // ===== งานของทุกคนในระบบ (ให้ job แจ้งเตือนของเปรมใช้) =====
    // ดึง plant + user + species มาด้วย เพราะ job ต้องรู้ว่าต้นไม้เป็นของใครเพื่อส่งแจ้งเตือน
    // และ job รันนอกหน้าเว็บ ถ้าไม่ fetch มาก่อนจะเรียก schedule.getPlant().getUser() ไม่ได้

    // ทุกคน: ช่วงวันที่ from..to (รวมทั้งสองวัน)
    @Query("""
            select cs from CareSchedule cs
            join fetch cs.plant p
            join fetch p.user
            join fetch p.species
            where cs.isActive = true
              and cs.nextDueDate between :from and :to
            order by cs.nextDueDate asc, cs.id asc
            """)
    List<CareSchedule> findAllActiveDueBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    // ทุกคน: เลยกำหนดแล้ว nextDueDate < date
    @Query("""
            select cs from CareSchedule cs
            join fetch cs.plant p
            join fetch p.user
            join fetch p.species
            where cs.isActive = true
              and cs.nextDueDate < :date
            order by cs.nextDueDate asc, cs.id asc
            """)
    List<CareSchedule> findAllActiveDueBefore(@Param("date") LocalDate date);

    // หาตาราง 1 อัน แต่ต้องเป็นของต้นไม้ของผู้ใช้คนนี้เท่านั้น
    // (กันกดบันทึกของคนอื่น)
    @Query("""
            select cs from CareSchedule cs
            join fetch cs.plant p
            join fetch p.species
            where cs.id = :id and p.user.id = :userId
            """)
    Optional<CareSchedule> findByIdAndOwner(@Param("id") Long id, @Param("userId") Long userId);
}