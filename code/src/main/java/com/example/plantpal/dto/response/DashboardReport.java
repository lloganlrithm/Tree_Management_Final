package com.example.plantpal.dto.response;

import com.example.plantpal.domain.enums.Severity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

// รายงานสุขภาพ 1 รายการที่รอ admin ตอบ
@Getter
@AllArgsConstructor
public class DashboardReport {
    private Long id;
    private String title;
    private String plantName;
    private Severity severity;
    private LocalDateTime createdAt;
}
