package com.example.plantpal.service.strategy;

import com.example.plantpal.domain.entity.Species;
import com.example.plantpal.domain.enums.ActionType;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Context ของ Strategy: รวม strategy ทุกตัวไว้ แล้วเลือกตัวที่ตรงกับประเภทงาน
// Spring ส่ง CareIntervalStrategy ทุกตัวที่เป็น @Component มาให้เอง
// อยากเพิ่มงานประเภทใหม่ แค่สร้างคลาส strategy ใหม่ ไม่ต้องแก้คลาสนี้
@Component
public class CareIntervalCalculator {

    // งานที่ยังไม่มี strategy ของตัวเอง (เช็กแดด / ตรวจสุขภาพ) ใช้รอบนี้ไปก่อน
    static final int FALLBACK_DAYS = 7;

    private final Map<ActionType, CareIntervalStrategy> strategies = new EnumMap<>(ActionType.class);

    public CareIntervalCalculator(List<CareIntervalStrategy> strategyList) {
        for (CareIntervalStrategy strategy : strategyList) {
            strategies.put(strategy.actionType(), strategy);
        }
    }

    // งานที่มี strategy (= งานที่สร้างตารางอัตโนมัติตอนเพิ่มต้นไม้): WATER,
    // FERTILIZE, REPOT
    public Set<ActionType> supportedTypes() {
        return strategies.keySet();
    }

    // ทำงานนี้ทุกกี่วัน
    public int intervalDays(ActionType type, Species species) {
        CareIntervalStrategy strategy = strategies.get(type);
        return (strategy != null) ? strategy.intervalDays(species) : FALLBACK_DAYS;
    }

    // วันกำหนดครั้งถัดไป นับจากวัน from
    public LocalDate nextDueDate(ActionType type, Species species, LocalDate from) {
        return from.plusDays(intervalDays(type, species));
    }
}