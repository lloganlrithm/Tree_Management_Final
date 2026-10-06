package com.example.plantpal.service.impl;

import com.example.plantpal.domain.entity.Species;
import com.example.plantpal.dto.request.SpeciesRequest;
import com.example.plantpal.repository.PlantRepository;
import com.example.plantpal.repository.SpeciesRepository;
import com.example.plantpal.service.SpeciesService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SpeciesServiceImpl implements SpeciesService {

    private final SpeciesRepository speciesRepository;
    private final PlantRepository plantRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Species> findAll() {
        return speciesRepository.findAll(Sort.by("name"));
    }

    @Override
    public Species save(SpeciesRequest request) {
        String name = request.getName().trim();

        // ชื่อห้ามซ้ำ (ยกเว้นตัวที่กำลังแก้ไขอยู่เอง)
        speciesRepository.findByName(name)
                .filter(found -> !found.getId().equals(request.getId()))
                .ifPresent(found -> {
                    throw new IllegalArgumentException("มีพันธุ์ไม้ชื่อ \"" + name + "\" อยู่แล้ว");
                });

        Species species = (request.getId() == null)
                ? new Species()
                : speciesRepository.findById(request.getId())
                        .orElseThrow(() -> new IllegalArgumentException("ไม่พบพันธุ์ไม้ที่ต้องการแก้ไข"));

        species.setName(name);
        species.setWaterIntervalDays(request.getWaterIntervalDays());
        species.setFertilizeIntervalDays(request.getFertilizeIntervalDays());
        species.setRepotIntervalDays(request.getRepotIntervalDays());
        species.setSunlightRequirement(request.getSunlightRequirement());
        species.setDescription(StringUtils.hasText(request.getDescription())
                ? request.getDescription().trim()
                : null);

        return speciesRepository.save(species);
    }

    @Override
    public void delete(Long id) {
        if (!speciesRepository.existsById(id)) {
            throw new IllegalArgumentException("ไม่พบพันธุ์ไม้ที่ต้องการลบ");
        }
        // plants.species_id เป็น ON DELETE RESTRICT ถ้ามีต้นไม้ใช้อยู่ DB จะไม่ยอมลบ เลยเช็กก่อนเพื่อแจ้งข้อความที่อ่านง่าย
        if (plantRepository.existsBySpeciesId(id)) {
            throw new IllegalStateException("ลบไม่ได้ เพราะมีต้นไม้ของผู้ใช้ใช้พันธุ์นี้อยู่");
        }
        speciesRepository.deleteById(id);
    }
}