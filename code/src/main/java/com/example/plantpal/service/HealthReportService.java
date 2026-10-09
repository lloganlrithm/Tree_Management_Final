package com.example.plantpal.service;

import com.example.plantpal.domain.entity.HealthReport;
import com.example.plantpal.domain.enums.HealthStatus;
import com.example.plantpal.domain.enums.ReportStatus;
import com.example.plantpal.domain.enums.Severity;
import com.example.plantpal.dto.request.HealthReportRequest;
import com.example.plantpal.dto.request.ReportFollowUpRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface HealthReportService {

    // ---- ผู้ใช้ ----
    HealthReport create(HealthReportRequest request, String email);

    // status = null คือทุกสถานะ
    Page<HealthReport> findMyReports(String email, ReportStatus status, Pageable pageable);

    HealthReport findMyReport(Long id, String email);

    // ลบได้เฉพาะรายงานของตัวเอง
    void deleteMyReport(Long id, String email);

    // ---- ผู้ใช้อัปเดตผลหลัง admin ให้คำแนะนำ (ใช้ได้เฉพาะรายงาน "กำลังดำเนินการ") ----
    // ต้นไม้ดีขึ้นแล้ว: รายงานเป็น "ต้นไม้ดีขึ้นแล้ว" + ต้นไม้เป็น "กำลังฟื้นตัว"
    HealthReport markImproved(Long id, String email);

    // ยังไม่ดีขึ้น: รอบเดิมเป็น "ส่งต่อรอบใหม่" แล้วเปิดรายงานใหม่ (ติดตามผล) ต้นเดียวกัน ให้ admin ตรวจอีกรอบ คืนรายงานใหม่
    HealthReport followUp(Long id, ReportFollowUpRequest request, String email);

    // จำนวนรายงานที่ยังรอ admin ตอบ (โป้ยใช้ใน dashboard)
    long countPending(String email);

    // รายงานของต้นไม้ 1 ต้น ใช้ใน fragment หน้ารายละเอียดต้นไม้
    List<HealthReport> findByPlant(Long plantId, String email);

    // ---- ระบบ (job รายวัน) ----
    // รายงาน "กำลังดำเนินการ" ที่สร้างก่อน before
    List<HealthReport> findStaleInProgress(LocalDateTime before);

    // รายงานที่เงียบนานเป็น "หมดเวลาติดตามผล" + แจ้งเจ้าของ
    void autoClose(HealthReport report, int staleDays);

    // ---- Admin ----
    HealthReport findById(Long id);

    // status / severity = null คือไม่กรอง
    Page<HealthReport> findAll(ReportStatus status, Severity severity, Pageable pageable);

    // หน้า admin: 1 แถวต่อ 1 เรื่อง (ไม่เลือกสถานะ = ซ่อนรอบที่ส่งต่อไปแล้ว) เลือกสถานะ = แสดงสถานะนั้นตรงๆ
    Page<HealthReport> findLatestRounds(ReportStatus status, Severity severity, Pageable pageable);

    // รอบก่อนหน้าของเรื่องเดียวกัน key = id รายงาน, value = รอบที่ส่งต่อมาถึงรายงานนี้ ใหม่สุดก่อน (ไม่มี = list ว่าง)
    Map<Long, List<HealthReport>> findPreviousRounds(List<HealthReport> latest);

    // plantHealth = null คือเปลี่ยนสถานะต้นไม้อัตโนมัติตามสถานะรายงาน, มีค่า = admin เลือกเอง
    HealthReport reply(Long id, ReportStatus status, String adminReply, HealthStatus plantHealth);

    default HealthReport reply(Long id, ReportStatus status, String adminReply) {
        return reply(id, status, adminReply, null);
    }
}
