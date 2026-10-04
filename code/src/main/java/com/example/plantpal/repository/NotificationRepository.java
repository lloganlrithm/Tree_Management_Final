package com.example.plantpal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.plantpal.domain.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    long countByUserIdAndIsReadFalse(Long userId);
}
