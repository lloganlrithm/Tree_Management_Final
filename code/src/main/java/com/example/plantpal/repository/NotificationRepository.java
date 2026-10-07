package com.example.plantpal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.plantpal.domain.entity.Notification;

// @EntityGraph("plant") ดึงต้นไม้มาด้วย หน้าเว็บจะได้ทำลิงก์ไปต้นไม้ได้ (open-in-view ปิดอยู่)
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    long countByUserIdAndIsReadFalse(Long userId);

    @EntityGraph(attributePaths = "plant")
    Page<Notification> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = "plant")
    Page<Notification> findByUserIdAndIsReadFalse(Long userId, Pageable pageable);

    Optional<Notification> findByIdAndUserId(Long id, Long userId);

    // อัปเดตทีเดียวทั้งก้อนใน DB ไม่ต้องโหลดมาทีละแถว คืนจำนวนแถวที่เปลี่ยน
    @Modifying
    @Query("update Notification n set n.isRead = true where n.user.id = :userId and n.isRead = false")
    int markAllAsRead(@Param("userId") Long userId);
}
