package com.example.plantpal.mapper;

import com.example.plantpal.domain.entity.HealthReport;
import com.example.plantpal.domain.entity.Plant;
import com.example.plantpal.dto.response.HealthReportResponse;
import org.springframework.stereotype.Component;

// แปลง Entity -> Response DTO ไว้ที่เดียว (Mapper ของ DTO Pattern)
// Controller ไม่ต้องรู้ว่า Entity มี field อะไรบ้าง แค่เรียก toResponse()
@Component
public class HealthReportMapper {

    public HealthReportResponse toResponse(HealthReport report) {
        Plant plant = report.getPlant();
        return new HealthReportResponse(
                report.getId(),
                plant.getId(),
                plant.getNickname(),
                report.getTitle(),
                report.getDescription(),
                report.getSeverity(),
                report.getImageUrl(),
                report.getStatus(),
                report.getAdminReply(),
                report.getCreatedAt(),
                report.getResolvedAt());
    }
}
