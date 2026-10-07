package com.example.plantpal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
