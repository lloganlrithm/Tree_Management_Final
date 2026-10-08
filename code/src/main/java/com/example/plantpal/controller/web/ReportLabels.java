package com.example.plantpal.controller.web;

import com.example.plantpal.domain.enums.HealthStatus;
import com.example.plantpal.domain.enums.NotificationType;
import com.example.plantpal.domain.enums.ReportStatus;
import com.example.plantpal.domain.enums.Severity;
import com.example.plantpal.plant.state.PlantHealthStates;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

// แปลง enum เป็นข้อความภาษาไทยสำหรับหน้าเว็บ ใช้ใน Thymeleaf ด้วย ${@reportLabels.status(r.status)}
// รวมไว้ที่เดียว ทุกหน้าของรายงาน/แจ้งเตือนจะได้ใช้คำเดียวกัน
@Component("reportLabels")
public class ReportLabels {

    // ใช้วน chip ตัวกรองสถานะ
    public ReportStatus[] statuses() {
        return ReportStatus.values();
    }

    public Severity[] severities() {
        return Severity.values();
    }

    public HealthStatus[] healthStatuses() {
        return HealthStatus.values();
    }

    // สถานะต้นไม้ที่เปลี่ยนไปได้จากสถานะปัจจุบัน (ถาม State ของโป้ย) ส่งเป็น "SICK,DEAD" ให้ script ในหน้า admin
    public String nextHealthCsv(HealthStatus current) {
        return PlantHealthStates.nextOf(current).stream().map(Enum::name).collect(Collectors.joining(","));
    }

    // ใช้คำเดียวกับหน้ารายละเอียดต้นไม้ของโป้ย
    public String health(HealthStatus status) {
        if (status == null) return "";
        return switch (status) {
            case HEALTHY -> "ปกติดี";
            case SICK -> "ป่วย";
            case RECOVERING -> "กำลังฟื้นตัว";
            case DEAD -> "ตายแล้ว";
        };
    }

    public String status(ReportStatus status) {
        if (status == null) return "";
        return switch (status) {
            case PENDING -> "รอตรวจ";
            case IN_PROGRESS -> "กำลังดำเนินการ";
            case RESOLVED -> "แก้ไขแล้ว";
            case REJECTED -> "ปฏิเสธ";
        };
    }

    public String severity(Severity severity) {
        if (severity == null) return "ไม่ระบุ";
        return switch (severity) {
            case LOW -> "เล็กน้อย";
            case MEDIUM -> "ปานกลาง";
            case HIGH -> "รุนแรง";
        };
    }

    // class ของกล่องไอคอนหน้ารายการ (icon-tile ใน base.css)
    public String severityTile(Severity severity) {
        if (severity == null) return "";
        return switch (severity) {
            case LOW -> "";
            case MEDIUM -> "warn";
            case HIGH -> "crit";
        };
    }

    public String notificationType(NotificationType type) {
        if (type == null) return "";
        return switch (type) {
            case CARE_DUE -> "ถึงเวลาดูแล";
            case HEALTH_REPLY -> "ตอบรายงาน";
            case SYSTEM -> "ระบบ";
        };
    }
}
