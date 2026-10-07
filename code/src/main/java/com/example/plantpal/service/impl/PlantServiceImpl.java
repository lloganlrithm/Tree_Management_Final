package com.example.plantpal.service.impl;   // ← แก้ให้ตรงกับ SpeciesServiceImpl

import com.example.plantpal.domain.entity.Plant;
import com.example.plantpal.domain.entity.Species;
import com.example.plantpal.domain.entity.User;
import com.example.plantpal.dto.request.PlantRequest;
import com.example.plantpal.repository.PlantRepository;
import com.example.plantpal.repository.SpeciesRepository;
import com.example.plantpal.repository.UserRepository;
import com.example.plantpal.service.PlantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PlantServiceImpl implements PlantService {

    private final PlantRepository plantRepository;
    private final SpeciesRepository speciesRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Plant> findMyPlants(String email, String keyword) {
        // ไม่ได้พิมพ์คำค้น -> เอาทั้งหมด
        if (keyword == null || keyword.isBlank()) {
            return plantRepository.findByUserEmailOrderByCreatedAtDesc(email);
        }
        return plantRepository
                .findByUserEmailAndNicknameContainingIgnoreCaseOrderByCreatedAtDesc(email, keyword.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public Plant findMyPlant(Long id, String email) {
        return plantRepository.findByIdAndUserEmail(id, email)
                .orElseThrow(() -> new IllegalArgumentException("ไม่พบต้นไม้นี้"));
    }

    @Override
    public Plant create(PlantRequest request, String email) {
        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("ไม่พบผู้ใช้"));

        Plant plant = new Plant();
        plant.setUser(owner);
        applyForm(plant, request);   // healthStatus = HEALTHY ตาม default ใน entity
        return plantRepository.save(plant);
    }

    @Override
    public Plant update(PlantRequest request, String email) {
        Plant plant = findMyPlant(request.getId(), email);   // เช็คความเป็นเจ้าของไปในตัว
        applyForm(plant, request);
        return plantRepository.save(plant);
    }

    @Override
    public void delete(Long id, String email) {
        Plant plant = findMyPlant(id, email);
        plantRepository.delete(plant);   // cascade ลบ care/report ของต้นนี้ตาม
    }

    // ใช้ร่วมกันระหว่าง create และ update: คัดค่าจากฟอร์มลง entity
    private void applyForm(Plant plant, PlantRequest request) {
        Species species = speciesRepository.findById(request.getSpeciesId())
                .orElseThrow(() -> new IllegalArgumentException("ไม่พบพันธุ์ไม้ที่เลือก"));

        plant.setSpecies(species);
        plant.setNickname(request.getNickname());
        plant.setPlantedDate(request.getPlantedDate());
    }
}