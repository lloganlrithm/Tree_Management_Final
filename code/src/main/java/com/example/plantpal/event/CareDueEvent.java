package com.example.plantpal.event;

import com.example.plantpal.domain.enums.ActionType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

// Observer Pattern: ประกาศว่ามีงานดูแลใกล้ถึง / เลยกำหนด (ส่งจาก Job รายวัน)
// NotificationListener รับไปสร้างแจ้งเตือนประเภท CARE_DUE
// เก็บแค่ค่าที่ต้องใช้ ไม่ส่ง entity (ผู้ฟังทำงานหลัง transaction ปิดแล้ว)
@Getter
@AllArgsConstructor
public class CareDueEvent {

    private final Long ownerId;        // เจ้าของต้นไม้ = คนที่จะได้รับแจ้งเตือน
    private final Long plantId;
    private final String plantName;
    private final ActionType actionType;
    private final LocalDate dueDate;
    private final boolean overdue;     // true = เลยกำหนดแล้ว, false = ใกล้ถึงกำหนด
}
