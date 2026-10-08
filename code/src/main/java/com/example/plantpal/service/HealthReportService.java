package com.example.plantpal.service;

import com.example.plantpal.domain.entity.HealthReport;
import com.example.plantpal.domain.enums.HealthStatus;
import com.example.plantpal.domain.enums.ReportStatus;
import com.example.plantpal.domain.enums.Severity;
import com.example.plantpal.dto.request.HealthReportRequest;
import com.example.plantpal.dto.request.ReportFollowUpRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface HealthReportService {

    // ---- ผู้ใช้ ----
    HealthReport create(HealthReportRequest request, String email);

    // status = null คือทุกสถานะ
    Page<HealthReport> findMyReports(String email, ReportStatus status, Pageable pageable);

    HealthReport findMyReport(Long id, String email);

    // ลบได้เฉพาะรายงานของตัวเอง
    void deleteMyReport(Long id, String email);

    // ---- ผู้ใช้อัปเดตผลหลัง admin ให้คำแนะนำ (ใช้ได้เฉพาะรายงาน "กำลังดำเนินการ") ----
    // ต้นไม้ดีขึ้นแล้ว: ปิดเรื่อง + ต้นไม้เป็น "กำลังฟื้นตัว"
    HealthReport markImproved(Long id, String email);

    // ยังไม่ดีขึ้น: ปิดรอบเดิม แล้วเปิดรายงานใหม่ (ติดตามผล) ต้นเดียวกัน ให้ admin ตรวจอีกรอบ คืนรายงานใหม่
    HealthReport followUp(Long id, ReportFollowUpRequest request, String email);

    // จำนวนรายงานที่ยังรอ admin ตอบ (โป้ยใช้ใน dashboard)
    long countPending(String email);

    // รายงานของต้นไม้ 1 ต้น ใช้ใน fragment หน้ารายละเอียดต้นไม้
    List<HealthReport> findByPlant(Long plantId, String email);

    // ---- Admin ----
    HealthReport findById(Long id);

    // status / severity = null คือไม่กรอง
    Page<HealthReport> findAll(ReportStatus status, Severity severity, Pageable pageable);

    // plantHealth = null คือเปลี่ยนสถานะต้นไม้อัตโนมัติตามสถานะรายงาน, มีค่า = admin เลือกเอง
    HealthReport reply(Long id, ReportStatus status, String adminReply, HealthStatus plantHealth);

    default HealthReport reply(Long id, ReportStatus status, String adminReply) {
        return reply(id, status, adminReply, null);
    }
}
