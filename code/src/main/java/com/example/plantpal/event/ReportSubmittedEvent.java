package com.example.plantpal.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

// Observer Pattern: ผู้ใช้ส่งรายงานเข้ามาให้ admin ตรวจ (แจ้งปัญหาใหม่ หรือกด "ยังไม่ดีขึ้น")
// NotificationListener รับไปแจ้งเตือน admin ทุกคน HealthReportService ไม่ต้องรู้ว่า admin มีใครบ้าง
@Getter
@AllArgsConstructor
public class ReportSubmittedEvent {

    private final Long reportId;
    private final Long ownerId;       // เจ้าของต้นไม้ (ถ้าเป็น admin เองไม่ต้องแจ้งตัวเอง)
    private final String plantName;
    private final String reportTitle;
    private final boolean followUp;   // true = รอบติดตามผล (ผู้ใช้กด "ยังไม่ดีขึ้น")
}
