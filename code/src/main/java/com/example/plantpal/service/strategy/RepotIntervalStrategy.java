package com.example.plantpal.service.strategy;

import com.example.plantpal.domain.entity.Species;
import com.example.plantpal.domain.enums.ActionType;
import org.springframework.stereotype.Component;

// เปลี่ยนกระถาง: ใช้ repot_interval_days ของพันธุ์ ถ้า admin ไม่ได้กรอกใช้ค่าตั้งต้น 365 วัน
@Component
public class RepotIntervalStrategy implements CareIntervalStrategy {

    static final int DEFAULT_DAYS = 365;

    @Override
    public ActionType actionType() {
        return ActionType.REPOT;
    }

    @Override
    public int intervalDays(Species species) {
        Integer days = species.getRepotIntervalDays();
        return (days != null && days > 0) ? days : DEFAULT_DAYS;
    }
}