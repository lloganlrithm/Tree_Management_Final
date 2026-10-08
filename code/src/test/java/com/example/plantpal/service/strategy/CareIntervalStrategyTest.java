package com.example.plantpal.service.strategy;

import com.example.plantpal.domain.entity.Species;
import com.example.plantpal.domain.enums.ActionType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// ทดสอบ Strategy คำนวณรอบของแต่ละงาน: ใช้รอบจากพันธุ์ไม้ ถ้าว่าง/ไม่ถูกต้องใช้ค่าตั้งต้น
class CareIntervalStrategyTest {

    private Species species(Integer water, Integer fertilize, Integer repot) {
        return Species.builder()
                .name("มอนสเตอร่า")
                .waterIntervalDays(water)
                .fertilizeIntervalDays(fertilize)
                .repotIntervalDays(repot)
                .build();
    }

    // ===== รดน้ำ =====

    @Test
    void waterUsesSpeciesWaterInterval() {
        WaterIntervalStrategy strategy = new WaterIntervalStrategy();

        assertThat(strategy.actionType()).isEqualTo(ActionType.WATER);
        assertThat(strategy.intervalDays(species(3, null, null))).isEqualTo(3);
    }

    // ===== ใส่ปุ๋ย =====

    @Test
    void fertilizeUsesSpeciesIntervalWhenSet() {
        FertilizeIntervalStrategy strategy = new FertilizeIntervalStrategy();

        assertThat(strategy.actionType()).isEqualTo(ActionType.FERTILIZE);
        assertThat(strategy.intervalDays(species(7, 14, null))).isEqualTo(14);
    }

    @Test
    void fertilizeUsesDefaultWhenSpeciesIntervalIsNull() {
        FertilizeIntervalStrategy strategy = new FertilizeIntervalStrategy();

        assertThat(strategy.intervalDays(species(7, null, null))).isEqualTo(30);
    }

    @Test
    void fertilizeUsesDefaultWhenSpeciesIntervalIsZero() {
        FertilizeIntervalStrategy strategy = new FertilizeIntervalStrategy();

        assertThat(strategy.intervalDays(species(7, 0, null))).isEqualTo(30);
    }

    // ===== เปลี่ยนกระถาง =====

    @Test
    void repotUsesSpeciesIntervalWhenSet() {
        RepotIntervalStrategy strategy = new RepotIntervalStrategy();

        assertThat(strategy.actionType()).isEqualTo(ActionType.REPOT);
        assertThat(strategy.intervalDays(species(7, null, 180))).isEqualTo(180);
    }

    @Test
    void repotUsesDefaultWhenSpeciesIntervalIsNull() {
        RepotIntervalStrategy strategy = new RepotIntervalStrategy();

        assertThat(strategy.intervalDays(species(7, null, null))).isEqualTo(365);
    }
}