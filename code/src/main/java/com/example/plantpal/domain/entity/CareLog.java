package com.example.plantpal.domain.entity;

import com.example.plantpal.domain.enums.ActionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "care_logs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CareLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;

    // nullable: รดน้ำนอกตารางก็บันทึกได้
    // ลบตารางดูแล -> log เก่ายังอยู่ แค่ค่านี้เป็น NULL (ON DELETE SET NULL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "care_schedule_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private CareSchedule careSchedule;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 20)
    private ActionType actionType;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "performed_at", nullable = false)
    @Builder.Default
    private LocalDateTime performedAt = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String notes;
}
