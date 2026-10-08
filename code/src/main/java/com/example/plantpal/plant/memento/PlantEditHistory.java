package com.example.plantpal.plant.memento;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Caretaker: เก็บ snapshot ล่าสุดของแต่ละต้นไม้
 * @SessionScope = ผู้ใช้แต่ละคนมีที่เก็บของตัวเอง (ไม่ปนกัน, ไม่ต้องแก้ DB)
 */
@Component
@SessionScope
public class PlantEditHistory implements Serializable {

    private final Map<Long, PlantSnapshot> lastEdits = new HashMap<>();

    /** เก็บค่าก่อนแก้ไข (ทับของเดิม = ย้อนได้ 1 ครั้งล่าสุด) */
    public void save(PlantSnapshot snapshot) {
        lastEdits.put(snapshot.plantId(), snapshot);
    }

    /** ต้นนี้มีให้ย้อนไหม (ใช้ซ่อน/แสดงปุ่ม) */
    public boolean canUndo(Long plantId) {
        return lastEdits.containsKey(plantId);
    }

    /** หยิบ snapshot ออกมาใช้ แล้วลบทิ้ง (ย้อนแล้วย้อนซ้ำไม่ได้) */
    public Optional<PlantSnapshot> take(Long plantId) {
        return Optional.ofNullable(lastEdits.remove(plantId));
    }
}