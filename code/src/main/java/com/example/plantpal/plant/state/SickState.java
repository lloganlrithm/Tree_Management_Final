package com.example.plantpal.plant.state;

import com.example.plantpal.domain.entity.Plant;
import com.example.plantpal.domain.enums.HealthStatus;

/** ป่วย: เริ่มฟื้นตัว, หายเป็นปกติ หรือตาย */
public class SickState implements PlantHealthState {

    @Override
    public HealthStatus status() {
        return HealthStatus.SICK;
    }

    @Override
    public boolean canChangeTo(HealthStatus target) {
        return target == HealthStatus.RECOVERING
                || target == HealthStatus.HEALTHY
                || target == HealthStatus.DEAD;
    }

    @Override
    public void onLeave(Plant plant, HealthStatus target) {
        if (target == HealthStatus.HEALTHY) {
            plant.setRecoveryCount(plant.getRecoveryCount() + 1);   // หายป่วย = ฟื้น 1 ครั้ง
        }
    }
}
