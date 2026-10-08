package com.example.plantpal.dto.response;

import com.example.plantpal.domain.enums.HealthStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

// ข้อมูลต้นไม้ที่ REST API ส่งออกไป (DTO Pattern)
// ไม่ส่ง Entity Plant ตรงๆ เพราะจะลาก User (มีรหัสผ่าน) และ list ลูกๆ (care, report) ติดออกไปด้วย
// และถ้าวันหลังแก้ Entity รูปแบบ JSON ที่คนเรียก API ใช้อยู่ก็ไม่เปลี่ยนตาม
public record PlantResponse(
        Long id,
        String nickname,
        Long speciesId,
        String speciesName,
        HealthStatus healthStatus,
        Integer recoveryCount,
        LocalDate plantedDate,
        LocalDateTime createdAt
) {
}
