package com.example.plantpal.service.strategy;

import com.example.plantpal.domain.entity.Species;
import com.example.plantpal.domain.enums.ActionType;

// Strategy: วิธีคำนวณ "รอบการดูแล" ของงานแต่ละประเภท
// งานแต่ละแบบ (รดน้ำ / ใส่ปุ๋ย / เปลี่ยนกระถาง) มีคลาสของตัวเอง
// คนที่เรียกใช้ (CareIntervalCalculator) ไม่ต้องรู้ว่าแต่ละงานคำนวณยังไง
public interface CareIntervalStrategy {

    // งานประเภทไหนที่ strategy นี้รับผิดชอบ
    ActionType actionType();

    // ทำงานนี้ทุกกี่วัน สำหรับพันธุ์ไม้นี้ (ใช้รอบจาก species ถ้าว่างใช้ค่าตั้งต้น)
    int intervalDays(Species species);
}