package com.example.plantpal.service.impl;

import com.example.plantpal.domain.entity.Plant;
import com.example.plantpal.domain.entity.Species;
import com.example.plantpal.dto.request.PlantRequest;
import com.example.plantpal.plant.memento.PlantEditHistory;
import com.example.plantpal.repository.PlantRepository;
import com.example.plantpal.repository.SpeciesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Test ของ Memento pattern: แก้ไขต้นไม้แล้วย้อนกลับเป็นค่าเดิม
@ExtendWith(MockitoExtension.class)
class PlantServiceImplUndoTest {

    private static final String OWNER = "user@plantpal.com";
    private static final String OTHER = "other@plantpal.com";

    @Mock
    private PlantRepository plantRepository;

    @Mock
    private SpeciesRepository speciesRepository;

    // ใช้ Caretaker ตัวจริง (ไม่ mock) เพื่อเทสว่าเก็บและคืน snapshot ได้จริง
    @Spy
    private PlantEditHistory editHistory = new PlantEditHistory();

    @InjectMocks
    private PlantServiceImpl plantService;

    private Species monstera;
    private Species cactus;
    private Plant plant;

    @BeforeEach
    void setUp() {
        monstera = new Species();
        monstera.setId(1L);
        monstera.setName("มอนสเตอร่า");

        cactus = new Species();
        cactus.setId(2L);
        cactus.setName("กระบองเพชร");

        plant = new Plant();
        plant.setId(10L);
        plant.setSpecies(monstera);
        plant.setNickname("kk");
        plant.setPlantedDate(LocalDate.of(2026, 1, 1));
    }

    // แก้ไขชื่อ พันธุ์ และวันที่ปลูก
    private void editPlant() {
        PlantRequest request = new PlantRequest();
        request.setId(10L);
        request.setSpeciesId(2L);
        request.setNickname("ชื่อใหม่");
        request.setPlantedDate(LocalDate.of(2026, 5, 5));
        plantService.update(request, OWNER);
    }

    @Test
    void undoRestoresValuesBeforeEdit() {
        when(plantRepository.findByIdAndUserEmail(10L, OWNER)).thenReturn(Optional.of(plant));
        when(speciesRepository.findById(2L)).thenReturn(Optional.of(cactus));
        when(speciesRepository.findById(1L)).thenReturn(Optional.of(monstera));
        when(plantRepository.save(any(Plant.class))).thenAnswer(inv -> inv.getArgument(0));

        editPlant();
        assertThat(plant.getNickname()).isEqualTo("ชื่อใหม่");
        assertThat(plantService.canUndo(10L)).isTrue();   // แก้แล้ว -> มีให้ย้อน

        Plant result = plantService.undoLastEdit(10L, OWNER);

        assertThat(result.getNickname()).isEqualTo("kk");                         // กลับเป็นค่าเดิม
        assertThat(result.getSpecies()).isEqualTo(monstera);
        assertThat(result.getPlantedDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(plantService.canUndo(10L)).isFalse();  // ย้อนแล้วย้อนซ้ำไม่ได้
    }

    @Test
    void undoWithoutHistoryFails() {
        when(plantRepository.findByIdAndUserEmail(10L, OWNER)).thenReturn(Optional.of(plant));

        assertThatThrownBy(() -> plantService.undoLastEdit(10L, OWNER))
                .hasMessage("ไม่มีการแก้ไขให้ย้อน");
        verify(plantRepository, never()).save(any());
        assertThat(plant.getNickname()).isEqualTo("kk");   // ค่าไม่เปลี่ยน
    }

    @Test
    void cannotUndoOtherUsersPlant() {
        when(plantRepository.findByIdAndUserEmail(10L, OWNER)).thenReturn(Optional.of(plant));
        when(speciesRepository.findById(2L)).thenReturn(Optional.of(cactus));
        when(plantRepository.save(any(Plant.class))).thenAnswer(inv -> inv.getArgument(0));
        editPlant();   // เจ้าของแก้ไข -> มี snapshot

        when(plantRepository.findByIdAndUserEmail(10L, OTHER)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> plantService.undoLastEdit(10L, OTHER))
                .hasMessage("ไม่พบต้นไม้นี้");
        assertThat(plant.getNickname()).isEqualTo("ชื่อใหม่");   // คนอื่นย้อนไม่ได้ ค่าไม่เปลี่ยน
        assertThat(plantService.canUndo(10L)).isTrue();         // snapshot ของเจ้าของยังอยู่
    }
}