package com.example.plantpal.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.plantpal.domain.entity.HealthReport;
import com.example.plantpal.domain.enums.ReportStatus;
import com.example.plantpal.domain.enums.Severity;

// @EntityGraph("plant") ดึงต้นไม้มาพร้อมรายงาน เพราะ open-in-view ปิดอยู่ หน้าเว็บจะอ่าน plant.nickname ไม่ได้ถ้าไม่ดึงมาก่อน
public interface HealthReportRepository extends JpaRepository<HealthReport, Long> {
    List<HealthReport> findByPlantIdOrderByCreatedAtDesc(Long plantId);
    List<HealthReport> findByStatus(ReportStatus status);

    // ---- ผู้ใช้: เห็นเฉพาะรายงานของต้นไม้ตัวเอง ----
    @EntityGraph(attributePaths = "plant")
    Page<HealthReport> findByPlantUserEmail(String email, Pageable pageable);

    @EntityGraph(attributePaths = "plant")
    Page<HealthReport> findByPlantUserEmailAndStatus(String email, ReportStatus status, Pageable pageable);

    @EntityGraph(attributePaths = "plant")
    Optional<HealthReport> findByIdAndPlantUserEmail(Long id, String email);

    long countByPlantUserEmailAndStatus(String email, ReportStatus status);

    // รายงานสถานะนี้ที่สร้างก่อนเวลาที่กำหนด (job ปิดรายงานที่เงียบนาน) ดึงต้นไม้ + เจ้าของมาด้วยเพื่อแจ้งเตือน
    @EntityGraph(attributePaths = { "plant", "plant.user" })
    List<HealthReport> findByStatusAndCreatedAtBefore(ReportStatus status, LocalDateTime before);

    // รายงานล่าสุดของต้นนี้ที่ยังไม่ปิด (ใช้กันแจ้งซ้ำ)
    Optional<HealthReport> findFirstByPlantIdAndStatusInOrderByCreatedAtDesc(Long plantId, Collection<ReportStatus> statuses);

    @EntityGraph(attributePaths = "plant")
    List<HealthReport> findByPlantIdAndPlantUserEmailOrderByCreatedAtDesc(Long plantId, String email);

    // ---- Admin: เห็นทุกรายงาน กรองตามสถานะ/ความรุนแรง ----
    @EntityGraph(attributePaths = "plant")
    Page<HealthReport> findAllBy(Pageable pageable);

    @EntityGraph(attributePaths = "plant")
    Page<HealthReport> findByStatus(ReportStatus status, Pageable pageable);

    @EntityGraph(attributePaths = "plant")
    Page<HealthReport> findBySeverity(Severity severity, Pageable pageable);

    @EntityGraph(attributePaths = "plant")
    Page<HealthReport> findByStatusAndSeverity(ReportStatus status, Severity severity, Pageable pageable);

    @EntityGraph(attributePaths = { "plant", "plant.user" })
    Optional<HealthReport> findWithPlantById(Long id);

    // ---- Admin: เห็นแค่รอบล่าสุดของแต่ละต้น (รอบติดตามผลไม่ซ้อนกับรอบเก่า) ----
    // รอบล่าสุด = id มากสุดของต้นนั้น (id เพิ่มตามลำดับการสร้าง)
    String LATEST_ROUND = "select r from HealthReport r"
            + " where r.id = (select max(r2.id) from HealthReport r2 where r2.plant = r.plant)";
    String LATEST_ROUND_COUNT = "select count(r) from HealthReport r"
            + " where r.id = (select max(r2.id) from HealthReport r2 where r2.plant = r.plant)";

    @EntityGraph(attributePaths = "plant")
    @Query(value = LATEST_ROUND, countQuery = LATEST_ROUND_COUNT)
    Page<HealthReport> findLatestRounds(Pageable pageable);

    @EntityGraph(attributePaths = "plant")
    @Query(value = LATEST_ROUND + " and r.status = :status", countQuery = LATEST_ROUND_COUNT + " and r.status = :status")
    Page<HealthReport> findLatestRoundsByStatus(@Param("status") ReportStatus status, Pageable pageable);

    @EntityGraph(attributePaths = "plant")
    @Query(value = LATEST_ROUND + " and r.severity = :severity", countQuery = LATEST_ROUND_COUNT + " and r.severity = :severity")
    Page<HealthReport> findLatestRoundsBySeverity(@Param("severity") Severity severity, Pageable pageable);

    @EntityGraph(attributePaths = "plant")
    @Query(value = LATEST_ROUND + " and r.status = :status and r.severity = :severity",
            countQuery = LATEST_ROUND_COUNT + " and r.status = :status and r.severity = :severity")
    Page<HealthReport> findLatestRoundsByStatusAndSeverity(@Param("status") ReportStatus status,
                                                           @Param("severity") Severity severity, Pageable pageable);

    // ทุกรอบของหลายต้นในครั้งเดียว (ใช้แสดง "รอบก่อนหน้า" ในแผงตอบ admin ไม่ต้องยิง query ทีละต้น)
    List<HealthReport> findByPlantIdInOrderByCreatedAtDesc(Collection<Long> plantIds);
}
