package com.example.plantpal.service.impl;

import com.example.plantpal.domain.entity.CareSchedule;
import com.example.plantpal.domain.entity.HealthReport;
import com.example.plantpal.domain.entity.Plant;
import com.example.plantpal.domain.enums.HealthStatus;
import com.example.plantpal.domain.enums.ReportStatus;
import com.example.plantpal.dto.response.DashboardReport;
import com.example.plantpal.dto.response.DashboardTask;
import com.example.plantpal.repository.CareScheduleRepository;
import com.example.plantpal.repository.HealthReportRepository;
import com.example.plantpal.service.DashboardService;
import com.example.plantpal.service.PlantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final PlantService plantService;
    // ใช้ method ที่มีอยู่แล้วใน develop ไม่แก้ไฟล์ repository ของเพื่อน
    private final CareScheduleRepository careScheduleRepository;
    private final HealthReportRepository healthReportRepository;

    @Override
    public Map<String, Long> countPlantsByStatus(String email) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (HealthStatus status : HealthStatus.values()) {
            counts.put(status.name(), 0L);   // ทุกสถานะเริ่มที่ 0
        }
        plantService.findMyPlants(email, null)
                .forEach(p -> counts.merge(p.getHealthStatus().name(), 1L, Long::sum));
        return counts;
    }

    @Override
    public List<DashboardTask> findCareTasks(String email, LocalDate until) {
        LocalDate today = LocalDate.now();
        return careScheduleRepository.findByIsActiveTrueAndNextDueDateLessThanEqual(until).stream()
                .filter(s -> isOwner(s.getPlant(), email))                    // เฉพาะต้นไม้ของคนนี้
                .sorted(Comparator.comparing(CareSchedule::getNextDueDate))   // ใกล้กำหนดก่อน
                .map(s -> new DashboardTask(
                        s.getPlant().getId(),
                        displayName(s.getPlant()),
                        s.getPlant().getSpecies().getName(),
                        s.getActionType(),
                        s.getNextDueDate(),
                        s.getNextDueDate().isBefore(today)))
                .toList();
    }

    @Override
    public List<DashboardReport> findPendingReports(String email) {
        return healthReportRepository.findByStatus(ReportStatus.PENDING).stream()
                .filter(r -> isOwner(r.getPlant(), email))
                .sorted(Comparator.comparing(HealthReport::getCreatedAt).reversed())   // ใหม่สุดก่อน
                .map(r -> new DashboardReport(
                        r.getId(),
                        r.getTitle(),
                        displayName(r.getPlant()),
                        r.getSeverity(),
                        r.getCreatedAt()))
                .toList();
    }

    private boolean isOwner(Plant plant, String email) {
        return plant.getUser().getEmail().equals(email);
    }

    // ไม่มีชื่อเล่นใช้ชื่อพันธุ์แทน (เหมือนในการ์ดต้นไม้)
    private String displayName(Plant plant) {
        String nickname = plant.getNickname();
        return (nickname != null && !nickname.isBlank()) ? nickname : plant.getSpecies().getName();
    }
}
