package com.example.plantpal.service;

import com.example.plantpal.domain.entity.HealthReport;
import com.example.plantpal.domain.enums.ReportStatus;
import com.example.plantpal.domain.enums.Severity;
import com.example.plantpal.dto.request.HealthReportRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface HealthReportService {

    // ---- ผู้ใช้ ----
    HealthReport create(HealthReportRequest request, String email);

    // status = null คือทุกสถานะ
    Page<HealthReport> findMyReports(String email, ReportStatus status, Pageable pageable);

    HealthReport findMyReport(Long id, String email);

    // รายงานของต้นไม้ 1 ต้น ใช้ใน fragment หน้ารายละเอียดต้นไม้
    List<HealthReport> findByPlant(Long plantId, String email);

    // ---- Admin ----
    // status / severity = null คือไม่กรอง
    Page<HealthReport> findAll(ReportStatus status, Severity severity, Pageable pageable);

    HealthReport reply(Long id, ReportStatus status, String adminReply);
}
