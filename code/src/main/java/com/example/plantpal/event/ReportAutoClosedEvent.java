package com.example.plantpal.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

// Observer Pattern: ระบบปิดรายงานที่ "กำลังดำเนินการ" แต่ผู้ใช้ไม่ได้บอกผลนานเกินกำหนด (ส่งจาก StaleReportCloseJob)
// NotificationListener รับไปแจ้งเจ้าของต้นไม้ว่ารายงานถูกปิดแล้ว
@Getter
@AllArgsConstructor
public class ReportAutoClosedEvent {

    private final Long reportId;
    private final Long ownerId;
    private final Long plantId;
    private final String reportTitle;
    private final int staleDays;
}
