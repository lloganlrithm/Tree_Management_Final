package com.example.plantpal.service.strategy;

import com.example.plantpal.domain.entity.Species;
import com.example.plantpal.domain.enums.ActionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// ทดสอบ Context ของ Strategy: เลือก strategy ให้ตรงกับประเภทงาน
class CareIntervalCalculatorTest {

    private CareIntervalCalculator calculator;
    private Species species;

    @BeforeEach
    void setUp() {
        // ส่ง strategy ทั้ง 3 ตัวเข้าไปเหมือนที่ Spring ทำให้ตอนรันจริง
        calculator = new CareIntervalCalculator(List.of(
                new WaterIntervalStrategy(),
                new FertilizeIntervalStrategy(),
                new RepotIntervalStrategy()));
        species = Species.builder()
                .name("ลิ้นมังกร")
                .waterIntervalDays(7)
                .fertilizeIntervalDays(null)
                .repotIntervalDays(288)
                .build();
    }

    @Test
    void picksStrategyByActionType() {
        assertThat(calculator.intervalDays(ActionType.WATER, species)).isEqualTo(7);
        assertThat(calculator.intervalDays(ActionType.FERTILIZE, species)).isEqualTo(30);   // ว่าง → ค่าตั้งต้น
        assertThat(calculator.intervalDays(ActionType.REPOT, species)).isEqualTo(288);
    }

    @Test
    void usesFallbackForTypesWithoutStrategy() {
        assertThat(calculator.intervalDays(ActionType.CHECK_SUNLIGHT, species)).isEqualTo(7);
        assertThat(calculator.intervalDays(ActionType.HEALTH_CHECK, species)).isEqualTo(7);
    }

    @Test
    void supportedTypesAreTheAutoCreatedSchedules() {
        assertThat(calculator.supportedTypes())
                .containsExactlyInAnyOrder(ActionType.WATER, ActionType.FERTILIZE, ActionType.REPOT);
    }

    @Test
    void nextDueDateAddsIntervalToStartDate() {
        LocalDate from = LocalDate.of(2026, 10, 8);

        assertThat(calculator.nextDueDate(ActionType.WATER, species, from))
                .isEqualTo(LocalDate.of(2026, 10, 15));
    }
}