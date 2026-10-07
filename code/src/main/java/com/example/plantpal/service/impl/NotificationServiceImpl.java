package com.example.plantpal.service.impl;

import com.example.plantpal.domain.entity.Notification;
import com.example.plantpal.domain.entity.Plant;
import com.example.plantpal.domain.entity.User;
import com.example.plantpal.domain.enums.NotificationType;
import com.example.plantpal.exception.ResourceNotFoundException;
import com.example.plantpal.repository.NotificationRepository;
import com.example.plantpal.service.NotificationService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    // getReference = อ้างถึง user/plant ด้วย id อย่างเดียว ไม่ต้อง query มาทั้งแถว
    // และไม่ต้องเรียก Repository ของโมดูลเพื่อน
    private final EntityManager entityManager;

    @Override
    public Notification create(Long userId, Long plantId, NotificationType type, String message) {
        Notification notification = new Notification();
        notification.setUser(entityManager.getReference(User.class, userId));
        if (plantId != null) {
            notification.setPlant(entityManager.getReference(Plant.class, plantId));
        }
        notification.setType(type);
        // คอลัมน์ message ยาวได้ 255 ตัวอักษร
        notification.setMessage(message.length() > 255 ? message.substring(0, 255) : message);
        // isRead = false ตาม default ใน entity
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> findMine(Long userId, boolean unreadOnly, Pageable pageable) {
        return unreadOnly
                ? notificationRepository.findByUserIdAndIsReadFalse(userId, pageable)
                : notificationRepository.findByUserId(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> findLatest(Long userId, int limit) {
        Pageable latest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return notificationRepository.findByUserId(userId, latest).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    public void markAsRead(Long id, Long userId) {
        // หาด้วย id + userId = อ่านได้เฉพาะแจ้งเตือนของตัวเอง
        Notification notification = notificationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบการแจ้งเตือนนี้"));
        notification.setIsRead(true);
    }

    @Override
    public int markAllAsRead(Long userId) {
        return notificationRepository.markAllAsRead(userId);
    }
}
