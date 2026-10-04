package com.example.plantpal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.plantpal.domain.entity.HealthReport;
import com.example.plantpal.domain.enums.ReportStatus;

public interface HealthReportRepository extends JpaRepository<HealthReport, Long> {
    List<HealthReport> findByPlantIdOrderByCreatedAtDesc(Long plantId);
    List<HealthReport> findByStatus(ReportStatus status);
}
