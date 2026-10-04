package com.example.plantpal.repository;

import com.example.plantpal.domain.entity.CareSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface CareScheduleRepository extends JpaRepository<CareSchedule, Long> {
    List<CareSchedule> findByPlantId(Long plantId);
    // ใช้ตอน scheduler หา "งานที่ถึงกำหนด"
    List<CareSchedule> findByIsActiveTrueAndNextDueDateLessThanEqual(LocalDate date);
}
