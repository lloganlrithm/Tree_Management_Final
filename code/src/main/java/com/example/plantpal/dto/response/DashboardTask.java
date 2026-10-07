package com.example.plantpal.dto.response;

import com.example.plantpal.domain.enums.ActionType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

// งานดูแล 1 รายการที่แสดงใน dashboard (คัดค่าที่หน้าเว็บใช้ไว้ตั้งแต่ใน service)
@Getter
@AllArgsConstructor
public class DashboardTask {
    private Long plantId;
    private String plantName;
    private String speciesName;
    private ActionType actionType;
    private LocalDate dueDate;
    private boolean overdue;   // เลยกำหนดแล้ว
}
