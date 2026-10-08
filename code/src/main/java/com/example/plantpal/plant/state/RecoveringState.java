package com.example.plantpal.plant.state;

import com.example.plantpal.domain.entity.Plant;
import com.example.plantpal.domain.enums.HealthStatus;

/** กำลังฟื้นตัว: หายเป็นปกติ, กลับไปป่วยซ้ำ หรือตาย */
public class RecoveringState implements PlantHealthState {

    @Override
    public HealthStatus status() {
        return HealthStatus.RECOVERING;
    }

    @Override
    public boolean canChangeTo(HealthStatus target) {
        return target == HealthStatus.HEALTHY
                || target == HealthStatus.SICK
                || target == HealthStatus.DEAD;
    }

    @Override
    public void onLeave(Plant plant, HealthStatus target) {
        if (target == HealthStatus.HEALTHY) {
            plant.setRecoveryCount(plant.getRecoveryCount() + 1);   // ฟื้นสำเร็จ 1 ครั้ง
        }
    }
}
