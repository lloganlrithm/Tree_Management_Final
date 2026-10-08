package com.example.plantpal.plant.memento;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Memento: ค่าของต้นไม้ "ก่อนแก้ไข" ใช้ย้อนกลับ
 * เป็น record จึงแก้ค่าข้างในไม่ได้หลังสร้างแล้ว
 * ไม่เก็บ healthStatus เพราะสถานะต้องเปลี่ยนผ่าน State pattern เท่านั้น
 */
public record PlantSnapshot(
        Long plantId,
        Long speciesId,
        String nickname,
        LocalDate plantedDate
) implements Serializable { }