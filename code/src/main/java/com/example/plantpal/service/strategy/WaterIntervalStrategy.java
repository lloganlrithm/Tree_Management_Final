package com.example.plantpal.service.strategy;

import com.example.plantpal.domain.entity.Species;
import com.example.plantpal.domain.enums.ActionType;
import org.springframework.stereotype.Component;

// รดน้ำ: ทุกพันธุ์ต้องมีรอบรดน้ำ (water_interval_days เป็น NOT NULL)
@Component
public class WaterIntervalStrategy implements CareIntervalStrategy {

    @Override
    public ActionType actionType() {
        return ActionType.WATER;
    }

    @Override
    public int intervalDays(Species species) {
        return species.getWaterIntervalDays();
    }
}