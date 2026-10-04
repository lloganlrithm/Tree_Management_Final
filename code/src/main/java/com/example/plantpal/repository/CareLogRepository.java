package com.example.plantpal.repository;

import com.example.plantpal.domain.entity.CareLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CareLogRepository extends JpaRepository<CareLog, Long> {
    List<CareLog> findByPlantIdOrderByPerformedAtDesc(Long plantId);
}
