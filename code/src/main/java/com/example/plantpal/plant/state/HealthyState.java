package com.example.plantpal.plant.state;

import com.example.plantpal.domain.enums.HealthStatus;

/** แข็งแรง: ป่วยได้ หรือตายได้ */
public class HealthyState implements PlantHealthState {

    @Override
    public HealthStatus status() {
        return HealthStatus.HEALTHY;
    }

    @Override
    public boolean canChangeTo(HealthStatus target) {
        return target == HealthStatus.SICK || target == HealthStatus.DEAD;
    }
}
