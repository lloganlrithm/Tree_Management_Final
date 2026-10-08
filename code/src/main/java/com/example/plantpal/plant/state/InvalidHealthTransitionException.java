package com.example.plantpal.plant.state;

import com.example.plantpal.domain.enums.HealthStatus;

/** เปลี่ยนสถานะสุขภาพแบบที่กติกาไม่อนุญาต เช่น DEAD -> HEALTHY */
public class InvalidHealthTransitionException extends RuntimeException {

    public InvalidHealthTransitionException(HealthStatus from, HealthStatus to) {
        super("เปลี่ยนสถานะจาก " + from + " เป็น " + to + " ไม่ได้");
    }
}
