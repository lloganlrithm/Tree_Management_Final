package com.example.plantpal.dto.response;

import com.example.plantpal.domain.enums.Role;

import java.time.LocalDateTime;

// 1 แถวในตารางหน้า admin จัดการผู้ใช้
public record AdminUserRow(
        Long id,
        String email,
        String fullName,
        String avatarUrl,
        Role role,
        boolean active,
        long plantCount,
        LocalDateTime createdAt,
        boolean self) {      // true = แถวของ admin ที่ login อยู่ (ห้ามแก้ตัวเอง)
}
