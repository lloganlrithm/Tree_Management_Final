package com.example.plantpal.domain.enums;

public enum ReportStatus {
    PENDING, IN_PROGRESS, RESOLVED, REJECTED,
    // รอบนี้จบแบบส่งต่อรายงานติดตามผล / ไม่มีการบอกผลจนหมดเวลา (V4 migration)
    FOLLOWED_UP, AUTO_CLOSED
}
