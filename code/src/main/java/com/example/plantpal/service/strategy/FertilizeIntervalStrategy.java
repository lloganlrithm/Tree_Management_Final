package com.example.plantpal.service.strategy;

import com.example.plantpal.domain.entity.Species;
import com.example.plantpal.domain.enums.ActionType;
import org.springframework.stereotype.Component;

// ใส่ปุ๋ย: ใช้ fertilize_interval_days ของพันธุ์ ถ้า admin ไม่ได้กรอกใช้ค่าตั้งต้น 30 วัน
@Component
public class FertilizeIntervalStrategy implements CareIntervalStrategy {

    static final int DEFAULT_DAYS = 30;

    @Override
    public ActionType actionType() {
        return ActionType.FERTILIZE;
    }

    @Override
    public int intervalDays(Species species) {
        Integer days = species.getFertilizeIntervalDays();
        return (days != null && days > 0) ? days : DEFAULT_DAYS;
    }
}