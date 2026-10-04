package com.example.plantpal.domain.entity;

import com.example.plantpal.domain.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ลบต้นไม้ -> แจ้งเตือนของต้นนั้นหายตาม 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Plant plant;

    // ลบตารางดูแล -> แจ้งเตือนยังอยู่ แค่ค่านี้เป็น NULL 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "care_schedule_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private CareSchedule careSchedule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationType type;

    @Column(nullable = false, length = 255)
    private String message;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
