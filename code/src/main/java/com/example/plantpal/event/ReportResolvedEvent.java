package com.example.plantpal.event;

import com.example.plantpal.domain.enums.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

// Observer Pattern: "ข่าว" ที่ HealthReportService ประกาศเมื่อ admin ตอบรายงาน
// คนส่งไม่ต้องรู้ว่าใครฟังอยู่ NotificationListener เป็นคนรับไปสร้างแจ้งเตือน
// เก็บแค่ค่าที่ต้องใช้ (ไม่ส่ง entity) เพราะผู้ฟังอาจทำงานหลัง transaction ปิดไปแล้ว
@Getter
@AllArgsConstructor
public class ReportResolvedEvent {

    private final Long reportId;
    private final Long ownerId;     // เจ้าของต้นไม้ = คนที่จะได้รับแจ้งเตือน
    private final Long plantId;
    private final String reportTitle;
    private final ReportStatus status;
}
