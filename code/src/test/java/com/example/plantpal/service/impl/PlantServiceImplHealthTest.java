package com.example.plantpal.service.impl;

import com.example.plantpal.domain.entity.Plant;
import com.example.plantpal.domain.enums.HealthStatus;
import com.example.plantpal.plant.state.InvalidHealthTransitionException;
import com.example.plantpal.repository.PlantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlantServiceImplHealthTest {

    @Mock
    private PlantRepository plantRepository;

    @InjectMocks
    private PlantServiceImpl plantService;

    private Plant plant(HealthStatus status) {
        Plant plant = new Plant();
        plant.setId(1L);
        plant.setHealthStatus(status);
        plant.setRecoveryCount(0);
        return plant;
    }

    @Test
    void markSickSavesNewStatus() {
        Plant plant = plant(HealthStatus.HEALTHY);
        when(plantRepository.findById(1L)).thenReturn(Optional.of(plant));
        when(plantRepository.save(plant)).thenReturn(plant);

        Plant result = plantService.markSick(1L);

        assertThat(result.getHealthStatus()).isEqualTo(HealthStatus.SICK);
        verify(plantRepository).save(plant);
    }

    @Test
    void changeMyPlantHealthChecksOwner() {
        when(plantRepository.findByIdAndUserEmail(1L, "other@plantpal.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> plantService.changeMyPlantHealth(1L, "other@plantpal.com", HealthStatus.SICK))
                .isInstanceOf(IllegalArgumentException.class);
        verify(plantRepository, never()).save(any());
    }

    @Test
    void invalidTransitionIsNotSaved() {
        when(plantRepository.findById(1L)).thenReturn(Optional.of(plant(HealthStatus.DEAD)));

        assertThatThrownBy(() -> plantService.markRecovering(1L))
                .isInstanceOf(InvalidHealthTransitionException.class);
        verify(plantRepository, never()).save(any());
    }
}
