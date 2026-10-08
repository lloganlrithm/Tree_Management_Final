package com.example.plantpal.dto.response;

import com.example.plantpal.domain.entity.CareLog;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

// ผลสรุปประวัติการดูแล ที่ CareService คำนวณให้ (Controller แค่ส่งต่อให้หน้าเว็บ)
@Getter
@AllArgsConstructor
public class CareHistorySummary {
    private List<CareLog> logs;   // บันทึกการดูแล ใหม่สุดก่อน
    private long onTimeCount;     // ทำภายในวันกำหนด (หรือก่อนกำหนด)
    private long lateCount;       // ทำหลังวันกำหนด
}