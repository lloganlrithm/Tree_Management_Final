package com.example.plantpal.service.impl;

import com.example.plantpal.domain.entity.HealthReport;
import com.example.plantpal.domain.entity.Plant;
import com.example.plantpal.domain.enums.ReportStatus;
import com.example.plantpal.domain.enums.Severity;
import com.example.plantpal.dto.request.HealthReportRequest;
import com.example.plantpal.event.ReportResolvedEvent;
import com.example.plantpal.repository.HealthReportRepository;
import com.example.plantpal.service.HealthReportService;
import com.example.plantpal.service.ImageStorageService;
import com.example.plantpal.service.PlantService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class HealthReportServiceImpl implements HealthReportService {

    private final HealthReportRepository healthReportRepository;
    // เรียกผ่าน Service ไม่เรียก PlantRepository ตรงๆ 
    private final PlantService plantService;
    private final ApplicationEventPublisher eventPublisher;
    private final ImageStorageService imageStorageService;

    @Override
    public HealthReport create(HealthReportRequest request, String email) {
        // findMyPlant โยน error ถ้าต้นไม้ไม่ใช่ของคนที่ login อยู่ = เช็คความเป็นเจ้าของไปในตัว
        Plant plant = plantService.findMyPlant(request.getPlantId(), email);

        HealthReport report = new HealthReport();
        report.setPlant(plant);
        report.setTitle(request.getTitle().trim());
        report.setDescription(blankToNull(request.getDescription()));
        report.setSeverity(request.getSeverity());
        // แนบรูปมา = อัปโหลดขึ้น Cloudinary ก่อน แล้วเก็บแค่ลิงก์ลง DB (ถ้าอัปไม่ผ่านจะ error และไม่บันทึกรายงาน)
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            report.setImageUrl(imageStorageService.upload(request.getImage(), "reports"));
        }
        // status = PENDING ตาม default ใน entity
        return healthReportRepository.save(report);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HealthReport> findMyReports(String email, ReportStatus status, Pageable pageable) {
        return (status == null)
                ? healthReportRepository.findByPlantUserEmail(email, pageable)
                : healthReportRepository.findByPlantUserEmailAndStatus(email, status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public HealthReport findMyReport(Long id, String email) {
        return healthReportRepository.findByIdAndPlantUserEmail(id, email)
                .orElseThrow(() -> new IllegalArgumentException("ไม่พบรายงานนี้"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HealthReport> findByPlant(Long plantId, String email) {
        return healthReportRepository.findByPlantIdAndPlantUserEmailOrderByCreatedAtDesc(plantId, email);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HealthReport> findAll(ReportStatus status, Severity severity, Pageable pageable) {
        if (status != null && severity != null) {
            return healthReportRepository.findByStatusAndSeverity(status, severity, pageable);
        }
        if (status != null) {
            return healthReportRepository.findByStatus(status, pageable);
        }
        if (severity != null) {
            return healthReportRepository.findBySeverity(severity, pageable);
        }
        return healthReportRepository.findAllBy(pageable);
    }

    @Override
    public HealthReport reply(Long id, ReportStatus status, String adminReply) {
        HealthReport report = healthReportRepository.findWithPlantById(id)
                .orElseThrow(() -> new IllegalArgumentException("ไม่พบรายงานนี้"));

        report.setStatus(status);
        report.setAdminReply(blankToNull(adminReply));
        // RESOLVED / REJECTED = ปิดเรื่อง บันทึกเวลาปิด ถ้าเปิดกลับมาให้ล้างเวลาปิดออก
        boolean closed = status == ReportStatus.RESOLVED || status == ReportStatus.REJECTED;
        report.setResolvedAt(closed ? LocalDateTime.now() : null);
        HealthReport saved = healthReportRepository.save(report);

        // Observer: ประกาศว่ามีการตอบรายงาน ใครฟังอยู่ก็ทำงานของตัวเองต่อ (เช่น สร้างแจ้งเตือนให้เจ้าของ)
        Plant plant = saved.getPlant();
        eventPublisher.publishEvent(new ReportResolvedEvent(
                saved.getId(), plant.getUser().getId(), plant.getId(), saved.getTitle(), status));
        return saved;
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
