package com.example.plantpal.service.impl;

import com.example.plantpal.domain.entity.CareLog;
import com.example.plantpal.domain.entity.CareSchedule;
import com.example.plantpal.domain.entity.Species;
import com.example.plantpal.domain.entity.User;
import com.example.plantpal.repository.CareLogRepository;
import com.example.plantpal.repository.CareScheduleRepository;
import com.example.plantpal.service.CareService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CareServiceImpl implements CareService {

    // รอบตั้งต้น (วัน) เมื่อพันธุ์ไม้ไม่ได้กรอกรอบไว้
    private static final int DEFAULT_FERTILIZE_DAYS = 30;
    private static final int DEFAULT_REPOT_DAYS = 365;
    private static final int DEFAULT_CHECK_SUNLIGHT_DAYS = 7;
    private static final int DEFAULT_HEALTH_CHECK_DAYS = 14;

    private final CareScheduleRepository careScheduleRepository;
    private final CareLogRepository careLogRepository;

    @Override
    public List<CareSchedule> findOverdue(User user) {
        return careScheduleRepository.findActiveDueBefore(user.getId(), LocalDate.now());
    }

    @Override
    public List<CareSchedule> findDueToday(User user) {
        LocalDate today = LocalDate.now();
        return careScheduleRepository.findActiveDueBetween(user.getId(), today, today);
    }

    @Override
    public List<CareSchedule> findUpcoming(User user) {
        return careScheduleRepository.findActiveDueAfter(user.getId(), LocalDate.now());
    }

    @Override
    public List<CareSchedule> findDueBetween(User user, LocalDate from, LocalDate to) {
        return careScheduleRepository.findActiveDueBetween(user.getId(), from, to);
    }

    @Override
    @Transactional
    public CareLog markDone(Long scheduleId, User user, String notes) {
        CareSchedule schedule = careScheduleRepository.findByIdAndOwner(scheduleId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("ไม่พบตารางดูแลนี้"));

        // 1) บันทึกประวัติ: due_date = วันที่กำหนดไว้เดิม (ก่อนเลื่อน)
        // ไว้เทียบว่าทำตรงเวลาหรือช้า
        CareLog log = CareLog.builder()
                .plant(schedule.getPlant())
                .careSchedule(schedule)
                .actionType(schedule.getActionType())
                .dueDate(schedule.getNextDueDate())
                .performedAt(LocalDateTime.now())
                .notes(notes == null || notes.isBlank() ? null : notes.trim())
                .build();
        careLogRepository.save(log);

        // 2) เลื่อนรอบถัดไป นับจากวันที่ทำจริง (วันนี้)
        schedule.setNextDueDate(LocalDate.now().plusDays(intervalDays(schedule)));
        // ไม่ต้อง save เอง: schedule อยู่ใน transaction นี้ Hibernate
        // อัปเดตให้ตอนจบเมธอด

        return log;
    }

    // รอบของงานแต่ละประเภท ใช้รอบจากพันธุ์ไม้ก่อน ถ้าไม่มีใช้ค่าตั้งต้น
    // TODO(C2): ถ้าทำ Strategy คำนวณรอบแล้ว ให้ย้ายมาเรียกจาก Strategy แทนเมธอดนี้
    private int intervalDays(CareSchedule schedule) {
        Species species = schedule.getPlant().getSpecies();
        return switch (schedule.getActionType()) {
            case WATER -> species.getWaterIntervalDays();
            case FERTILIZE -> orDefault(species.getFertilizeIntervalDays(), DEFAULT_FERTILIZE_DAYS);
            case REPOT -> orDefault(species.getRepotIntervalDays(), DEFAULT_REPOT_DAYS);
            case CHECK_SUNLIGHT -> DEFAULT_CHECK_SUNLIGHT_DAYS;
            case HEALTH_CHECK -> DEFAULT_HEALTH_CHECK_DAYS;
        };
    }

    private int orDefault(Integer value, int fallback) {
        return (value != null && value > 0) ? value : fallback;
    }
}