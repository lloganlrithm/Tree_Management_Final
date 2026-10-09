package com.example.plantpal.service;

import com.example.plantpal.domain.entity.Plant;
import com.example.plantpal.domain.enums.HealthStatus;
import com.example.plantpal.dto.request.PlantRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PlantService {

    List<Plant> findMyPlants(String email, String keyword);

    Page<Plant> findMyPlantsPage(String email, Pageable pageable);   // แบบแบ่งหน้า (REST API)

    Plant findMyPlant(Long id, String email);

    Plant create(PlantRequest request, String email);

    Plant update(PlantRequest request, String email);

    void delete(Long id, String email);
    // ===== ย้อนการแก้ไข (Memento pattern ใน plant/memento) =====

    /** ต้นนี้มีการแก้ไขให้ย้อนไหม (ใช้ซ่อน/แสดงปุ่มในหน้า detail) */
    boolean canUndo(Long id);

    /** ย้อนการแก้ไขล่าสุดของต้นไม้ตัวเอง กลับเป็นค่าก่อนแก้ */
    Plant undoLastEdit(Long id, String email);

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