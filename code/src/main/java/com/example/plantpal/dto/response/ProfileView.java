package com.example.plantpal.dto.response;

import com.example.plantpal.domain.enums.Role;

import java.time.LocalDateTime;

// ข้อมูลที่หน้าโปรไฟล์ใช้แสดง
public record ProfileView(
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        String avatarUrl,
        Role role,
        LocalDateTime createdAt,
        long plantCount,
        long careLogCount) {
}
