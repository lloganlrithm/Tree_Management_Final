package com.example.plantpal.service.impl;

import com.example.plantpal.domain.entity.CareLog;
import com.example.plantpal.domain.entity.CareSchedule;
import com.example.plantpal.domain.entity.Plant;
import com.example.plantpal.domain.entity.Species;
import com.example.plantpal.domain.entity.User;
import com.example.plantpal.domain.enums.ActionType;
import com.example.plantpal.dto.response.CareHistorySummary;
import com.example.plantpal.event.PlantCreatedEvent;
import com.example.plantpal.repository.CareLogRepository;
import com.example.plantpal.repository.CareScheduleRepository;
import com.example.plantpal.repository.PlantRepository;
import com.example.plantpal.service.strategy.CareIntervalCalculator;
import com.example.plantpal.service.strategy.FertilizeIntervalStrategy;
import com.example.plantpal.service.strategy.RepotIntervalStrategy;
import com.example.plantpal.service.strategy.WaterIntervalStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CareServiceImplTest {

    @Mock
    private CareScheduleRepository careScheduleRepository;
    @Mock
    private CareLogRepository careLogRepository;
    @Mock
    private PlantRepository plantRepository;

    private CareServiceImpl careService;
    private User user;
    private Plant plant;

    @BeforeEach
    void setUp() {
        // ใช้ Strategy ตัวจริง เพื่อทดสอบว่า CareService คำนวณรอบผ่าน Strategy ถูกต้อง
        CareIntervalCalculator calculator = new CareIntervalCalculator(List.of(
                new WaterIntervalStrategy(),
                new FertilizeIntervalStrategy(),
                new RepotIntervalStrategy()));
        careService = new CareServiceImpl(careScheduleRepository, careLogRepository, calculator, plantRepository);

        user = new User();
        user.setId(1L);

        Species species = Species.builder()
                .name("มอนสเตอร่า")
                .waterIntervalDays(3)
                .build();
        plant = new Plant();
        plant.setId(10L);
        plant.setUser(user);
        plant.setSpecies(species);
    }

    private CareSchedule waterSchedule(LocalDate due) {
        return CareSchedule.builder().id(100L).plant(plant).actionType(ActionType.WATER).nextDueDate(due).build();
    }

    private CareLog log(LocalDate due, LocalDateTime performedAt) {
        return CareLog.builder().plant(plant).actionType(ActionType.WATER).dueDate(due).performedAt(performedAt).build();
    }

    // ===== markDone (กด "ทำแล้ว") =====

    @Test
    void markDoneSavesLogWithOriginalDueDate() {
        LocalDate oldDue = LocalDate.now().minusDays(2);
        when(careScheduleRepository.findByIdAndOwner(100L, 1L)).thenReturn(Optional.of(waterSchedule(oldDue)));

        careService.markDone(100L, user, "ใบเริ่มเหลือง");

        ArgumentCaptor<CareLog> saved = ArgumentCaptor.forClass(CareLog.class);
        verify(careLogRepository).save(saved.capture());
        assertThat(saved.getValue().getDueDate()).isEqualTo(oldDue);
        assertThat(saved.getValue().getActionType()).isEqualTo(ActionType.WATER);
        assertThat(saved.getValue().getNotes()).isEqualTo("ใบเริ่มเหลือง");
    }

    @Test
    void markDoneMovesNextDueDateByStrategy() {
        CareSchedule schedule = waterSchedule(LocalDate.now().minusDays(2));
        when(careScheduleRepository.findByIdAndOwner(100L, 1L)).thenReturn(Optional.of(schedule));

        careService.markDone(100L, user, null);

        // รดน้ำทุก 3 วัน นับจากวันที่กดทำจริง
        assertThat(schedule.getNextDueDate()).isEqualTo(LocalDate.now().plusDays(3));
    }

    @Test
    void markDoneStoresBlankNotesAsNull() {
        when(careScheduleRepository.findByIdAndOwner(100L, 1L)).thenReturn(Optional.of(waterSchedule(LocalDate.now())));

        careService.markDone(100L, user, "   ");

        ArgumentCaptor<CareLog> saved = ArgumentCaptor.forClass(CareLog.class);
        verify(careLogRepository).save(saved.capture());
        assertThat(saved.getValue().getNotes()).isNull();
    }

    @Test
    void markDoneRejectsScheduleOfOtherUser() {
        when(careScheduleRepository.findByIdAndOwner(100L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> careService.markDone(100L, user, null))
                .isInstanceOf(IllegalArgumentException.class);
        verify(careLogRepository, never()).save(any());
    }

    // ===== ตรงเวลา / ช้า =====

    @Test
    void isLateWhenDoneAfterDueDate() {
        LocalDate due = LocalDate.of(2026, 10, 4);

        assertThat(careService.isLate(log(due, due.plusDays(4).atTime(18, 40)))).isTrue();
        assertThat(careService.isLate(log(due, due.atTime(23, 59)))).isFalse();       // วันเดียวกัน = ตรงเวลา
        assertThat(careService.isLate(log(due, due.minusDays(1).atTime(9, 0)))).isFalse(); // ก่อนกำหนด
        assertThat(careService.isLate(log(null, due.atTime(9, 0)))).isFalse();        // ไม่มีวันกำหนด
    }

    @Test
    void historySummaryCountsOnTimeAndLate() {
        LocalDate due = LocalDate.of(2026, 10, 4);
        List<CareLog> logs = List.of(
                log(due, due.plusDays(4).atStartOfDay()),   // ช้า
                log(due, due.atTime(10, 0)),                // ตรงเวลา
                log(due, due.minusDays(1).atTime(10, 0)),   // ก่อนกำหนด (นับเป็นตรงเวลา)
                log(null, due.atTime(10, 0)));              // ไม่มีวันกำหนด (ไม่นับทั้งสองแบบ)
        when(careLogRepository.findHistoryByOwner(1L)).thenReturn(logs);

        CareHistorySummary summary = careService.getHistorySummary(user, null);

        assertThat(summary.getLogs()).hasSize(4);
        assertThat(summary.getOnTimeCount()).isEqualTo(2);
        assertThat(summary.getLateCount()).isEqualTo(1);
    }

    @Test
    void historySummaryOfOnePlantUsesPlantQuery() {
        when(careLogRepository.findHistoryByPlantAndOwner(10L, 1L)).thenReturn(List.of());

        CareHistorySummary summary = careService.getHistorySummary(user, 10L);

        assertThat(summary.getLogs()).isEmpty();
        verify(careLogRepository, never()).findHistoryByOwner(any());
    }

    // ===== สร้างตารางอัตโนมัติเมื่อเพิ่มต้นไม้ (Observer + Strategy) =====

    @Test
    void onPlantCreatedCreatesWaterFertilizeAndRepotSchedules() {
        when(plantRepository.findById(10L)).thenReturn(Optional.of(plant));

        careService.onPlantCreated(new PlantCreatedEvent(10L));

        ArgumentCaptor<CareSchedule> saved = ArgumentCaptor.forClass(CareSchedule.class);
        verify(careScheduleRepository, times(3)).save(saved.capture());
        assertThat(saved.getAllValues())
                .extracting(CareSchedule::getActionType)
                .containsExactlyInAnyOrder(ActionType.WATER, ActionType.FERTILIZE, ActionType.REPOT);

        CareSchedule water = saved.getAllValues().stream()
                .filter(s -> s.getActionType() == ActionType.WATER).findFirst().orElseThrow();
        assertThat(water.getNextDueDate()).isEqualTo(LocalDate.now().plusDays(3));
        assertThat(water.getIsActive()).isTrue();
    }
}