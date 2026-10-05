package com.example.plantpal.domain.entity;

import com.example.plantpal.domain.enums.SunlightRequirement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "species")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Species {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "water_interval_days", nullable = false)
    private Integer waterIntervalDays;

    @Column(name = "fertilize_interval_days")
    private Integer fertilizeIntervalDays;

    @Column(name = "repot_interval_days")
    private Integer repotIntervalDays;

    @Enumerated(EnumType.STRING)
    @Column(name = "sunlight_requirement", length = 20)
    private SunlightRequirement sunlightRequirement;

    @Column(columnDefinition = "TEXT")
    private String description;

    // ไม่ทำ List<Plant> ฝั่งนี้ เพราะไม่จำเป็นต้องดึงต้นไม้ทั้งหมดจากพันธุ์
}
