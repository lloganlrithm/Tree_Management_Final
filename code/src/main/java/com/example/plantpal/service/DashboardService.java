package com.example.plantpal.service;

import com.example.plantpal.dto.response.DashboardReport;
import com.example.plantpal.dto.response.DashboardTask;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DashboardService {

    // นับต้นไม้แยกตามสถานะ เช่น {"HEALTHY": 3, "SICK": 1, ...}
    Map<String, Long> countPlantsByStatus(String email);

    // งานดูแลที่ครบกำหนดภายในวันที่ until (รวมที่เลยกำหนดแล้ว)
    List<DashboardTask> findCareTasks(String email, LocalDate until);

    // รายงานของผู้ใช้ที่ยังรอ admin ตอบ (PENDING)
    List<DashboardReport> findPendingReports(String email);
}
