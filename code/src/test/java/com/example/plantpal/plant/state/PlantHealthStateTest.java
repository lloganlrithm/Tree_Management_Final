package com.example.plantpal.plant.state;

import com.example.plantpal.domain.entity.Plant;
import com.example.plantpal.domain.enums.HealthStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlantHealthStateTest {

    private Plant plantWith(HealthStatus status) {
        Plant plant = new Plant();
        plant.setHealthStatus(status);
        plant.setRecoveryCount(0);
        return plant;
    }

    private void change(Plant plant, HealthStatus target) {
        PlantHealthStates.of(plant.getHealthStatus()).changeTo(plant, target);
    }

    @Test
    void healthyCanBecomeSick() {
        Plant plant = plantWith(HealthStatus.HEALTHY);

        change(plant, HealthStatus.SICK);

        assertThat(plant.getHealthStatus()).isEqualTo(HealthStatus.SICK);
        assertThat(plant.getRecoveryCount()).isZero();
    }

    @Test
    void fullRecoveryIncreasesRecoveryCount() {
        Plant plant = plantWith(HealthStatus.SICK);

        change(plant, HealthStatus.RECOVERING);
        change(plant, HealthStatus.HEALTHY);

        assertThat(plant.getHealthStatus()).isEqualTo(HealthStatus.HEALTHY);
        assertThat(plant.getRecoveryCount()).isEqualTo(1);
    }

    @Test
    void healthyCannotJumpToRecovering() {
        Plant plant = plantWith(HealthStatus.HEALTHY);

        assertThatThrownBy(() -> change(plant, HealthStatus.RECOVERING))
                .isInstanceOf(InvalidHealthTransitionException.class);
        assertThat(plant.getHealthStatus()).isEqualTo(HealthStatus.HEALTHY);
    }

    @Test
    void deadIsFinal() {
        Plant plant = plantWith(HealthStatus.DEAD);

        assertThatThrownBy(() -> change(plant, HealthStatus.HEALTHY))
                .isInstanceOf(InvalidHealthTransitionException.class);
        assertThat(PlantHealthStates.nextOf(HealthStatus.DEAD)).isEmpty();
    }

    @Test
    void sameStatusDoesNothing() {
        Plant plant = plantWith(HealthStatus.SICK);

        change(plant, HealthStatus.SICK);   // เช่น มีรายงานป่วยซ้ำ

        assertThat(plant.getHealthStatus()).isEqualTo(HealthStatus.SICK);
    }

    @Test
    void nextOfSickListsAllowedTargets() {
        assertThat(PlantHealthStates.nextOf(HealthStatus.SICK))
                .containsExactly(HealthStatus.HEALTHY, HealthStatus.RECOVERING, HealthStatus.DEAD);
    }
}
