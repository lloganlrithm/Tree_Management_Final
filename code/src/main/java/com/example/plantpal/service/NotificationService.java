package com.example.plantpal.service;

import com.example.plantpal.domain.entity.Notification;
import com.example.plantpal.domain.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {

    // ระบบสร้างให้ (เรียกจาก NotificationListener) plantId เป็น null ได้ เช่น แจ้งเตือนประเภท SYSTEM
    Notification create(Long userId, Long plantId, NotificationType type, String message);

    // ---- ผู้ใช้ที่ login อยู่ ----
    Page<Notification> findMine(Long userId, boolean unreadOnly, Pageable pageable);

    // รายการล่าสุดสำหรับ dropdown กระดิ่ง
    List<Notification> findLatest(Long userId, int limit);

    // จำนวนที่ยังไม่อ่าน แสดงบนกระดิ่งใน Navbar (พรีมใช้)
    long countUnread(Long userId);

    void markAsRead(Long id, Long userId);

    int markAllAsRead(Long userId);
}
