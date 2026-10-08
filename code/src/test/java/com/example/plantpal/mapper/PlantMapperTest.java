package com.example.plantpal.mapper;

import com.example.plantpal.domain.entity.Plant;
import com.example.plantpal.domain.entity.Species;
import com.example.plantpal.domain.entity.User;
import com.example.plantpal.domain.enums.HealthStatus;
import com.example.plantpal.dto.response.PlantResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

// ทดสอบ Mapper ของ DTO Pattern: แปลง Plant -> PlantResponse ครบทุก field
class PlantMapperTest {

    private final PlantMapper mapper = new PlantMapper();

    @Test
    void toResponseCopiesPlantAndSpeciesFields() {
        Species species = new Species();
        species.setId(3L);
        species.setName("มอนสเตอร่า");

        User owner = new User();
        owner.setEmail("user@plantpal.com");
        owner.setPassword("secret-hash");

        Plant plant = new Plant();
        plant.setId(7L);
        plant.setUser(owner);
        plant.setSpecies(species);
        plant.setNickname("ปา");
        plant.setHealthStatus(HealthStatus.SICK);
        plant.setRecoveryCount(2);
        plant.setPlantedDate(LocalDate.of(2026, 9, 1));
        plant.setCreatedAt(LocalDateTime.of(2026, 10, 7, 22, 0));

        PlantResponse response = mapper.toResponse(plant);

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.nickname()).isEqualTo("ปา");
        assertThat(response.speciesId()).isEqualTo(3L);
        assertThat(response.speciesName()).isEqualTo("มอนสเตอร่า");
        assertThat(response.healthStatus()).isEqualTo(HealthStatus.SICK);
        assertThat(response.recoveryCount()).isEqualTo(2);
        assertThat(response.plantedDate()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(response.createdAt()).isEqualTo(LocalDateTime.of(2026, 10, 7, 22, 0));
        // DTO ไม่มีข้อมูลผู้ใช้ (อีเมล/รหัสผ่าน) หลุดออกไป
        assertThat(response.toString()).doesNotContain("secret-hash", "user@plantpal.com");
    }
}
