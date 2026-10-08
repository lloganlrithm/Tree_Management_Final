package com.example.plantpal.service;

import com.example.plantpal.domain.entity.Plant;
import com.example.plantpal.domain.enums.HealthStatus;
import com.example.plantpal.dto.request.PlantRequest;

import java.util.List;

public interface PlantService {

    List<Plant> findMyPlants(String email, String keyword);

    Plant findMyPlant(Long id, String email);

    Plant create(PlantRequest request, String email);

    Plant update(PlantRequest request, String email);

    void delete(Long id, String email);

    // ===== สถานะสุขภาพ (State pattern ใน plant/state) =====

    /** เจ้าของเปลี่ยนสถานะต้นไม้ของตัวเองจากหน้ารายละเอียด */
    Plant changeMyPlantHealth(Long id, String email, HealthStatus target);

    /** ระบบอื่นเปลี่ยนสถานะ (เช่น รายงานสุขภาพของเปรม) ไม่เช็คเจ้าของ */
    Plant changeHealth(Long plantId, HealthStatus target);

    default Plant markSick(Long plantId) {
        return changeHealth(plantId, HealthStatus.SICK);
    }

    default Plant markRecovering(Long plantId) {
        return changeHealth(plantId, HealthStatus.RECOVERING);
    }
}