package com.example.plantpal.plant.state;

import com.example.plantpal.domain.enums.HealthStatus;

/** ตายแล้ว: สถานะสุดท้าย เปลี่ยนต่อไม่ได้ */
public class DeadState implements PlantHealthState {

    @Override
    public HealthStatus status() {
        return HealthStatus.DEAD;
    }

    @Override
    public boolean canChangeTo(HealthStatus target) {
        return false;
    }
}
