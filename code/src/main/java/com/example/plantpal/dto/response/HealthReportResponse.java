package com.example.plantpal.dto.response;

import com.example.plantpal.domain.enums.ReportStatus;
import com.example.plantpal.domain.enums.Severity;

import java.time.LocalDateTime;

// ข้อมูลรายงานที่ REST API ส่งออกไป (DTO Pattern)
// ไม่ส่ง Entity HealthReport ตรงๆ เพราะจะลาก Plant -> User (มีรหัสผ่าน) ติดออกไปด้วย
// และถ้าวันหลังแก้ Entity ก็ไม่ทำให้รูปแบบ JSON ที่คนเรียก API ใช้อยู่เปลี่ยนตาม
public record HealthReportResponse(
        Long id,
        Long plantId,
        String plantNickname,
        String title,
        String description,
        Severity severity,
        String imageUrl,
        ReportStatus status,
        String adminReply,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt
) {
}
