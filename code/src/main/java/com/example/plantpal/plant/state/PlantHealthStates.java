package com.example.plantpal.plant.state;

import com.example.plantpal.domain.enums.HealthStatus;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** หา object สถานะจากค่า health_status ที่เก็บใน DB (แต่ละสถานะมีตัวเดียวใช้ร่วมกัน) */
public final class PlantHealthStates {

    private static final Map<HealthStatus, PlantHealthState> STATES = new EnumMap<>(HealthStatus.class);

    static {
        register(new HealthyState());
        register(new SickState());
        register(new RecoveringState());
        register(new DeadState());
    }

    private PlantHealthStates() { }

    private static void register(PlantHealthState state) {
        STATES.put(state.status(), state);
    }

    public static PlantHealthState of(HealthStatus status) {
        return STATES.get(status == null ? HealthStatus.HEALTHY : status);
    }

    /** สถานะถัดไปที่เปลี่ยนได้จาก status (ใช้ทำปุ่มในหน้ารายละเอียด) */
    public static List<HealthStatus> nextOf(HealthStatus status) {
        PlantHealthState current = of(status);
        return Arrays.stream(HealthStatus.values())
                .filter(current::canChangeTo)
                .toList();
    }
}
