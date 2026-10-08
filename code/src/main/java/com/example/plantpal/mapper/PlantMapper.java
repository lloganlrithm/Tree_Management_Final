package com.example.plantpal.mapper;

import com.example.plantpal.domain.entity.Plant;
import com.example.plantpal.domain.entity.Species;
import com.example.plantpal.dto.response.PlantResponse;
import org.springframework.stereotype.Component;

// แปลง Entity Plant -> PlantResponse ไว้ที่เดียว (Mapper ของ DTO Pattern)
// Controller ไม่ต้องรู้ว่า Entity มี field อะไรบ้าง แค่เรียก toResponse()
// species ต้องถูกดึงมาพร้อม plant แล้ว (@EntityGraph ใน PlantRepository) เพราะ open-in-view=false
@Component
public class PlantMapper {

    public PlantResponse toResponse(Plant plant) {
        Species species = plant.getSpecies();
        return new PlantResponse(
                plant.getId(),
                plant.getNickname(),
                species.getId(),
                species.getName(),
                plant.getHealthStatus(),
                plant.getRecoveryCount(),
                plant.getPlantedDate(),
                plant.getCreatedAt());
    }
}
