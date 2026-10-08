package com.example.plantpal.plant.state;

import com.example.plantpal.domain.entity.Plant;
import com.example.plantpal.domain.enums.HealthStatus;

/**
 * State pattern: สถานะสุขภาพของต้นไม้
 * แต่ละสถานะรู้เองว่าเปลี่ยนไปสถานะไหนได้บ้าง
 *
 *   HEALTHY <-> SICK -> RECOVERING -> HEALTHY
 *   ทุกสถานะ (ยกเว้น DEAD) -> DEAD   และ DEAD เปลี่ยนต่อไม่ได้
 */
public interface PlantHealthState {

    HealthStatus status();

    /** สถานะนี้เปลี่ยนไปเป็น target ได้ไหม (ใช้ซ่อน/แสดงปุ่มในหน้าเว็บ) */
    boolean canChangeTo(HealthStatus target);

    /** เปลี่ยนสถานะของ plant ไปเป็น target ถ้าเปลี่ยนไม่ได้จะ throw InvalidHealthTransitionException */
    default void changeTo(Plant plant, HealthStatus target) {
        if (target == status()) {
            return;   // สถานะเดิม ไม่ต้องทำอะไร (เช่น มีรายงานป่วยซ้ำ)
        }
        if (!canChangeTo(target)) {
            throw new InvalidHealthTransitionException(status(), target);
        }
        onLeave(plant, target);
        plant.setHealthStatus(target);
    }

    /** งานเพิ่มเติมตอนออกจากสถานะนี้ (ค่าเริ่มต้น: ไม่มี) */
    default void onLeave(Plant plant, HealthStatus target) { }
}
