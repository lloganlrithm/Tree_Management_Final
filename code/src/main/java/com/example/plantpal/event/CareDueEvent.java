package com.example.plantpal.event;

import com.example.plantpal.domain.entity.CareSchedule;
import com.example.plantpal.domain.entity.Plant;
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

    // สร้าง event จากตารางดูแล 1 แถว (ใช้ร่วมกันใน job ทั้งสองตัว ต้องเรียกใน transaction เพราะอ่าน plant แบบ LAZY)
    public static CareDueEvent from(CareSchedule schedule, boolean overdue) {
        Plant plant = schedule.getPlant();
        String name = (plant.getNickname() != null && !plant.getNickname().isBlank())
                ? plant.getNickname()
                : plant.getSpecies().getName();   // ไม่มีชื่อเล่นใช้ชื่อพันธุ์แทน
        return new CareDueEvent(plant.getUser().getId(), plant.getId(), name,
                schedule.getActionType(), schedule.getNextDueDate(), overdue);
    }
}
