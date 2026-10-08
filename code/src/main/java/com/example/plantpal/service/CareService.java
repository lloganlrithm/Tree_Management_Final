package com.example.plantpal.service;

import com.example.plantpal.domain.entity.CareLog;
import com.example.plantpal.domain.entity.CareSchedule;
import com.example.plantpal.domain.entity.User;

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

    // กด "ทำแล้ว": บันทึกลง care_logs แล้วเลื่อน nextDueDate ไปรอบถัดไป
    CareLog markDone(Long scheduleId, User user, String notes);
}