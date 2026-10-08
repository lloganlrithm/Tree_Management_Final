package com.example.plantpal.mapper;

import com.example.plantpal.domain.entity.HealthReport;
import com.example.plantpal.domain.entity.Plant;
import com.example.plantpal.domain.enums.ReportStatus;
import com.example.plantpal.domain.enums.Severity;
import com.example.plantpal.dto.response.HealthReportResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

// ทดสอบ Mapper ของ DTO Pattern: แปลง Entity -> Response ครบทุก field และส่งแค่ข้อมูลต้นไม้ที่จำเป็น
class HealthReportMapperTest {

    private final HealthReportMapper mapper = new HealthReportMapper();

    @Test
    void toResponseCopiesAllFields() {
        Plant plant = new Plant();
        plant.setId(10L);
        plant.setNickname("ฟิโลหัวใจ");
        LocalDateTime created = LocalDateTime.of(2026, 10, 4, 9, 12);
        HealthReport report = new HealthReport();
        report.setId(1L);
        report.setPlant(plant);
        report.setTitle("ใบเหลือง");
        report.setDescription("ใบล่างเหลือง");
        report.setSeverity(Severity.HIGH);
        report.setImageUrl("https://res.cloudinary.com/demo/leaf.png");
        report.setStatus(ReportStatus.RESOLVED);
        report.setAdminReply("ลดการรดน้ำ");
        report.setCreatedAt(created);
        report.setResolvedAt(created.plusDays(1));

        HealthReportResponse response = mapper.toResponse(report);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.plantId()).isEqualTo(10L);
        assertThat(response.plantNickname()).isEqualTo("ฟิโลหัวใจ");
        assertThat(response.title()).isEqualTo("ใบเหลือง");
        assertThat(response.description()).isEqualTo("ใบล่างเหลือง");
        assertThat(response.severity()).isEqualTo(Severity.HIGH);
        assertThat(response.imageUrl()).isEqualTo("https://res.cloudinary.com/demo/leaf.png");
        assertThat(response.status()).isEqualTo(ReportStatus.RESOLVED);
        assertThat(response.adminReply()).isEqualTo("ลดการรดน้ำ");
        assertThat(response.createdAt()).isEqualTo(created);
        assertThat(response.resolvedAt()).isEqualTo(created.plusDays(1));
    }
}
