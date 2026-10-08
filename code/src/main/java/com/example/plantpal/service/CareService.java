package com.example.plantpal.service;

import com.example.plantpal.domain.entity.CareLog;
import com.example.plantpal.domain.entity.CareSchedule;
import com.example.plantpal.domain.entity.User;
import com.example.plantpal.dto.response.CareHistorySummary;

import java.time.LocalDate;
import java.util.List;

public interface CareService {

    // ตารางดูแลที่เลยกำหนดแล้ว (nextDueDate < วันนี้)
    List<CareSchedule> findOverdue(User user);

    // ตารางดูแลที่ถึงกำหนดวันนี้
    List<CareSchedule> findDueToday(User user);

    // ตารางดูแลที่ยังไม่ถึงกำหนด (nextDueDate > วันนี้)
    List<CareSchedule> findUpcoming(User user);

    // ตารางดูแลที่กำหนดอยู่ในช่วง from..to (รวมทั้งสองวัน) ให้หน้า dashboard ใช้ได้
    List<CareSchedule> findDueBetween(User user, LocalDate from, LocalDate to);

    // ===== งานของทุกคนในระบบ (ไม่ผูกกับผู้ใช้ที่ login) ให้ job แจ้งเตือนของเปรมใช้ =====

    // ตารางดูแลของทุกคนที่กำหนดอยู่ในช่วง from..to (รวมทั้งสองวัน)
    List<CareSchedule> findDueBetween(LocalDate from, LocalDate to);

    // ตารางดูแลของทุกคนที่เลยกำหนดแล้ว (nextDueDate < today)
    List<CareSchedule> findOverdue(LocalDate today);

    // ===== ประวัติการดูแล (C3) =====

    // ประวัติการดูแลต้นไม้ทุกต้นของผู้ใช้ ใหม่สุดก่อน
    List<CareLog> findHistory(User user);

    // ประวัติการดูแลของต้นไม้ 1 ต้น (ต้องเป็นของผู้ใช้คนนี้) ใหม่สุดก่อน
    List<CareLog> findPlantHistory(Long plantId, User user);

    // ประวัติการดูแลพร้อมสรุปจำนวนครั้งที่ตรงเวลา / ช้า
    // plantId = null → ทุกต้นของผู้ใช้, มีค่า → เฉพาะต้นนั้น (ต้องเป็นของผู้ใช้คนนี้)
    CareHistorySummary getHistorySummary(User user, Long plantId);

    // บันทึกนี้ทำช้ากว่าวันกำหนดไหม (ทำหลังวันกำหนด = ช้า, ไม่มีวันกำหนด = ไม่นับว่าช้า)
    boolean isLate(CareLog log);

    // ตารางดูแลที่ยังใช้งานของต้นไม้ 1 ต้น (ต้องเป็นของผู้ใช้คนนี้) ใกล้กำหนดก่อน
    List<CareSchedule> findPlantSchedules(Long plantId, User user);

    // กด "ทำแล้ว": บันทึกลง care_logs แล้วเลื่อน nextDueDate ไปรอบถัดไป
    CareLog markDone(Long scheduleId, User user, String notes);
}