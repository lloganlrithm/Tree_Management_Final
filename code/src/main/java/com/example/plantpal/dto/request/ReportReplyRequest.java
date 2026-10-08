package com.example.plantpal.dto.request;

import com.example.plantpal.domain.enums.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

// ข้อมูลที่ admin ส่งมาตอนตอบรายงาน (ใช้กับ PATCH /api/v1/reports/{id}/reply)
@Getter
@Setter
public class ReportReplyRequest {

    @NotNull(message = "กรุณาเลือกสถานะ")
    private ReportStatus status;

    private String adminReply;
}
