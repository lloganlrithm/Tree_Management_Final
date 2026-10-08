package com.example.plantpal.exception;

import lombok.Getter;

// ต้นไม้มีรายงานที่ยังไม่ปิดอยู่แล้ว แจ้งซ้ำไม่ได้ (GlobalExceptionHandler แปลงเป็น 409 Conflict)
// เก็บ id ของรายงานที่เปิดอยู่ไว้ หน้าเว็บจะได้ทำลิงก์พาไปรายงานเดิม
@Getter
public class DuplicateReportException extends RuntimeException {

    private final Long openReportId;

    public DuplicateReportException(String message, Long openReportId) {
        super(message);
        this.openReportId = openReportId;
    }
}
