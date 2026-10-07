package com.example.plantpal.event;

import com.example.plantpal.domain.entity.CareSchedule;
import com.example.plantpal.domain.entity.Plant;
import com.example.plantpal.domain.entity.Species;
import com.example.plantpal.domain.enums.ActionType;
import com.example.plantpal.repository.CareScheduleRepository;
import com.example.plantpal.repository.PlantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

// Observer: ฟัง PlantCreatedEvent แล้วสร้างตารางดูแลเริ่มต้นให้ต้นไม้ใหม่
// TODO(เปียโน C2): เปลี่ยนการคำนวณรอบเป็น Strategy แล้วลบไฟล์นี้ออก (ห้ามมีตัวรับ 2 ตัว
//                  เพราะ care_schedules มี UNIQUE(plant_id, action_type) จะสร้างซ้ำแล้ว error)
@Component
@RequiredArgsConstructor
public class PlantCreatedListener {

    private final PlantRepository plantRepository;
    private final CareScheduleRepository careScheduleRepository;

    @EventListener
    public void onPlantCreated(PlantCreatedEvent event) {
        // ทำงานใน transaction เดียวกับตอนเพิ่มต้นไม้ (PlantServiceImpl.create)
        Plant plant = plantRepository.findById(event.plantId()).orElseThrow();
        Species species = plant.getSpecies();
        LocalDate today = LocalDate.now();

        // รดน้ำ: ทุกพันธุ์มีรอบรดน้ำ (NOT NULL)
        addSchedule(plant, ActionType.WATER, today.plusDays(species.getWaterIntervalDays()));

        // ใส่ปุ๋ย / เปลี่ยนกระถาง: สร้างเฉพาะพันธุ์ที่ admin กรอกรอบไว้
        if (species.getFertilizeIntervalDays() != null) {
            addSchedule(plant, ActionType.FERTILIZE, today.plusDays(species.getFertilizeIntervalDays()));
        }
        if (species.getRepotIntervalDays() != null) {
            addSchedule(plant, ActionType.REPOT, today.plusDays(species.getRepotIntervalDays()));
        }
    }

    private void addSchedule(Plant plant, ActionType type, LocalDate dueDate) {
        careScheduleRepository.save(CareSchedule.builder()
                .plant(plant)
                .actionType(type)
                .nextDueDate(dueDate)
                .build());   // isActive = true ตาม default ใน entity
    }
}