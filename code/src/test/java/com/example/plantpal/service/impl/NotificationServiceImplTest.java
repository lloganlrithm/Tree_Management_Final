package com.example.plantpal.service.impl;

import com.example.plantpal.domain.entity.Notification;
import com.example.plantpal.domain.entity.Plant;
import com.example.plantpal.domain.entity.User;
import com.example.plantpal.domain.enums.NotificationType;
import com.example.plantpal.exception.ResourceNotFoundException;
import com.example.plantpal.repository.NotificationRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// ทดสอบ NotificationServiceImpl: DB และ EntityManager เป็นตัวปลอม
@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private EntityManager entityManager;

    @InjectMocks
    private NotificationServiceImpl service;

    // ---------- create ----------

    @Test
    void createSavesUnreadNotificationLinkedToUserAndPlant() {
        User user = User.builder().id(7L).build();
        Plant plant = new Plant();
        plant.setId(10L);
        when(entityManager.getReference(User.class, 7L)).thenReturn(user);
        when(entityManager.getReference(Plant.class, 10L)).thenReturn(plant);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        Notification result = service.create(7L, 10L, NotificationType.HEALTH_REPLY, "ตอบรายงานแล้ว");

        assertThat(result.getUser()).isSameAs(user);
        assertThat(result.getPlant()).isSameAs(plant);
        assertThat(result.getType()).isEqualTo(NotificationType.HEALTH_REPLY);
        assertThat(result.getMessage()).isEqualTo("ตอบรายงานแล้ว");
        assertThat(result.getIsRead()).isFalse();   // แจ้งเตือนใหม่ยังไม่อ่าน
    }

    @Test
    void createWithoutPlantLeavesPlantEmpty() {
        when(entityManager.getReference(User.class, 7L)).thenReturn(User.builder().id(7L).build());
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        Notification result = service.create(7L, null, NotificationType.SYSTEM, "ยินดีต้อนรับ");

        assertThat(result.getPlant()).isNull();
        verify(entityManager, never()).getReference(Plant.class, null);
    }

    @Test
    void createCutsMessageLongerThanColumnLimit() {
        when(entityManager.getReference(User.class, 7L)).thenReturn(User.builder().id(7L).build());
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        Notification result = service.create(7L, null, NotificationType.SYSTEM, "ก".repeat(300));

        assertThat(result.getMessage()).hasSize(255);   // notifications.message เป็น VARCHAR(255)
    }

    // ---------- findLatest ----------

    @Test
    void findLatestAsksForNewestFirstWithLimit() {
        when(notificationRepository.findByUserId(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        service.findLatest(7L, 5);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(notificationRepository).findByUserId(org.mockito.ArgumentMatchers.eq(7L), captor.capture());
        assertThat(captor.getValue().getPageSize()).isEqualTo(5);
        assertThat(captor.getValue().getSort().getOrderFor("createdAt").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    // ---------- markAsRead / markAllAsRead ----------

    @Test
    void markAsReadSetsOwnNotificationRead() {
        Notification notification = new Notification();
        notification.setIsRead(false);
        when(notificationRepository.findByIdAndUserId(1L, 7L)).thenReturn(Optional.of(notification));

        service.markAsRead(1L, 7L);

        assertThat(notification.getIsRead()).isTrue();
    }

    @Test
    void markAsReadOfOtherUserThrowsNotFound() {
        // หาด้วย id + userId: แจ้งเตือนของคนอื่นจะหาไม่เจอ
        when(notificationRepository.findByIdAndUserId(1L, 8L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.markAsRead(1L, 8L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("ไม่พบการแจ้งเตือนนี้");
    }

    @Test
    void markAllAsReadReturnsUpdatedCount() {
        when(notificationRepository.markAllAsRead(7L)).thenReturn(3);

        assertThat(service.markAllAsRead(7L)).isEqualTo(3);
    }
}
