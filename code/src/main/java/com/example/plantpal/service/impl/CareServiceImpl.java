package com.example.plantpal.service.impl;

import com.example.plantpal.domain.entity.CareLog;
import com.example.plantpal.domain.entity.CareSchedule;
import com.example.plantpal.domain.entity.Plant;
import com.example.plantpal.domain.entity.User;
import com.example.plantpal.domain.enums.ActionType;
import com.example.plantpal.event.PlantCreatedEvent;
import com.example.plantpal.repository.CareLogRepository;
import com.example.plantpal.repository.CareScheduleRepository;
import com.example.plantpal.repository.PlantRepository;
import com.example.plantpal.service.CareService;
import com.example.plantpal.service.strategy.CareIntervalCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CareServiceImpl implements CareService {

    private final CareScheduleRepository careScheduleRepository;
    private final CareLogRepository careLogRepository;
    private final CareIntervalCalculator intervalCalculator;   // Strategy คำนวณรอบ (service/strategy)
    private final PlantRepository plantRepository;

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

    // ===== งานของทุกคนในระบบ (ให้ job แจ้งเตือนของเปรมใช้) =====

    @Override
    public List<CareSchedule> findDueBetween(LocalDate from, LocalDate to) {
        return careScheduleRepository.findAllActiveDueBetween(from, to);
    }

    @Override
    public List<CareSchedule> findOverdue(LocalDate today) {
        return careScheduleRepository.findAllActiveDueBefore(today);
    }

    // ===== ประวัติการดูแล (C3) =====

    @Override
    public List<CareLog> findHistory(User user) {
        return careLogRepository.findHistoryByOwner(user.getId());
    }

    @Override
    public List<CareLog> findPlantHistory(Long plantId, User user) {
        return careLogRepository.findHistoryByPlantAndOwner(plantId, user.getId());
    }

    @Override
    public List<CareSchedule> findPlantSchedules(Long plantId, User user) {
        return careScheduleRepository.findActiveByPlantAndOwner(plantId, user.getId());
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

        // 2) เลื่อนรอบถัดไป นับจากวันที่ทำจริง (วันนี้) ใช้ Strategy ตามประเภทงาน
        schedule.setNextDueDate(intervalCalculator.nextDueDate(
                schedule.getActionType(), schedule.getPlant().getSpecies(), LocalDate.now()));
        // ไม่ต้อง save เอง: schedule อยู่ใน transaction นี้ Hibernate
        // อัปเดตให้ตอนจบเมธอด

        return log;
    }

    // Observer: ฟัง PlantCreatedEvent (โป๊ยส่งมาหลังเพิ่มต้นไม้ใน PlantServiceImpl.create)
    // แล้วสร้างตารางดูแลเริ่มต้น WATER / FERTILIZE / REPOT ให้ต้นไม้ใหม่
    // รอบของแต่ละงานคำนวณด้วย Strategy นับจากวันที่เพิ่มต้นไม้ (วันนี้)
    // ทำงานใน transaction เดียวกับตอนเพิ่มต้นไม้ ถ้าสร้างตารางไม่สำเร็จ ต้นไม้ก็จะไม่ถูกบันทึกด้วย
    @EventListener
    @Transactional
    public void onPlantCreated(PlantCreatedEvent event) {
        Plant plant = plantRepository.findById(event.plantId())
                .orElseThrow(() -> new IllegalArgumentException("ไม่พบต้นไม้ที่เพิ่งเพิ่ม"));
        LocalDate today = LocalDate.now();

        for (ActionType type : intervalCalculator.supportedTypes()) {
            careScheduleRepository.save(CareSchedule.builder()
                    .plant(plant)
                    .actionType(type)
                    .nextDueDate(intervalCalculator.nextDueDate(type, plant.getSpecies(), today))
                    .build());   // isActive = true ตาม default ใน entity
        }
    }
}