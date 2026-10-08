package com.example.plantpal.service.impl;

import com.example.plantpal.domain.entity.HealthReport;
import com.example.plantpal.domain.entity.Plant;
import com.example.plantpal.domain.enums.HealthStatus;
import com.example.plantpal.domain.enums.ReportStatus;
import com.example.plantpal.domain.enums.Severity;
import com.example.plantpal.dto.request.HealthReportRequest;
import com.example.plantpal.dto.request.ReportFollowUpRequest;
import com.example.plantpal.event.ReportResolvedEvent;
import com.example.plantpal.exception.DuplicateReportException;
import com.example.plantpal.exception.InvalidRequestException;
import com.example.plantpal.exception.ResourceNotFoundException;
import com.example.plantpal.plant.state.PlantHealthStates;
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

    // รายงานที่ยังไม่ปิด = รอตรวจ หรือ กำลังดำเนินการ
    private static final List<ReportStatus> OPEN_STATUSES = List.of(ReportStatus.PENDING, ReportStatus.IN_PROGRESS);
    private static final String FOLLOW_UP_PREFIX = "ติดตามผล: ";

    private final HealthReportRepository healthReportRepository;
    // เรียกผ่าน Service ไม่เรียก PlantRepository ตรงๆ 
    private final PlantService plantService;
    private final ApplicationEventPublisher eventPublisher;
    private final ImageStorageService imageStorageService;

    @Override
    public HealthReport create(HealthReportRequest request, String email) {
        // findMyPlant โยน error ถ้าต้นไม้ไม่ใช่ของคนที่ login อยู่ = เช็คความเป็นเจ้าของไปในตัว
        Plant plant = plantService.findMyPlant(request.getPlantId(), email);
        // ต้นไม้ 1 ต้นมีรายงานที่ยังเปิดอยู่ได้ทีละ 1 อัน (เช็คก่อนอัปโหลดรูป จะได้ไม่อัปรูปทิ้งเปล่าๆ)
        ensureNoOpenReport(plant.getId());

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
        // หาด้วย id + อีเมลเจ้าของ: รายงานของคนอื่นจะได้ "ไม่พบ" เหมือนไม่มีอยู่ (ไม่บอกว่ามีแต่ห้ามดู)
        return healthReportRepository.findByIdAndPlantUserEmail(id, email)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบรายงานนี้"));
    }

    @Override
    public void deleteMyReport(Long id, String email) {
        healthReportRepository.delete(findMyReport(id, email));
    }

    @Override
    public HealthReport markImproved(Long id, String email) {
        HealthReport report = findMyInProgressReport(id, email);
        report.setStatus(ReportStatus.RESOLVED);
        report.setResolvedAt(LocalDateTime.now());
        updatePlantHealth(report.getPlant(), ReportStatus.RESOLVED);   // State: ป่วย -> กำลังฟื้นตัว
        return healthReportRepository.save(report);
    }

    @Override
    public HealthReport followUp(Long id, ReportFollowUpRequest request, String email) {
        HealthReport previous = findMyInProgressReport(id, email);

        // อัปโหลดรูปก่อนแก้อะไรใน DB: ถ้าอัปไม่ผ่าน จะ error ออกไปโดยรอบเดิมยังเปิดอยู่เหมือนเดิม
        String imageUrl = null;
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            imageUrl = imageStorageService.upload(request.getImage(), "reports");
        }

        // ปิดรอบเดิม (รอบนี้ admin ตอบแล้ว ผลไปต่อในรายงานใหม่) ทำใน transaction เดียวกับการสร้างรายงานใหม่
        previous.setStatus(ReportStatus.RESOLVED);
        previous.setResolvedAt(LocalDateTime.now());
        healthReportRepository.save(previous);

        HealthReport next = new HealthReport();
        next.setPlant(previous.getPlant());
        next.setTitle(followUpTitle(previous.getTitle()));
        String message = blankToNull(request.getMessage());
        next.setDescription(message != null ? message : "ทำตามคำแนะนำแล้วแต่ยังไม่ดีขึ้น");
        next.setSeverity(previous.getSeverity());
        next.setImageUrl(imageUrl);
        // status = PENDING ตาม default ใน entity -> ขึ้นในหน้า admin เป็นรายงานรอตรวจ
        return healthReportRepository.save(next);
    }

    @Override
    @Transactional(readOnly = true)
    public long countPending(String email) {
        return healthReportRepository.countByPlantUserEmailAndStatus(email, ReportStatus.PENDING);
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
    @Transactional(readOnly = true)
    public HealthReport findById(Long id) {
        return healthReportRepository.findWithPlantById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบรายงานนี้"));
    }

    @Override
    public HealthReport reply(Long id, ReportStatus status, String adminReply, HealthStatus plantHealth) {
        HealthReport report = findById(id);

        report.setStatus(status);
        report.setAdminReply(blankToNull(adminReply));
        // RESOLVED / REJECTED = ปิดเรื่อง บันทึกเวลาปิด ถ้าเปิดกลับมาให้ล้างเวลาปิดออก
        boolean closed = status == ReportStatus.RESOLVED || status == ReportStatus.REJECTED;
        report.setResolvedAt(closed ? LocalDateTime.now() : null);
        HealthReport saved = healthReportRepository.save(report);

        Plant plant = saved.getPlant();
        if (plantHealth != null) {
            // admin เลือกเอง: ให้ State ของโป้ยตรวจ ถ้าเปลี่ยนไม่ได้จะโยน error แล้ว transaction ย้อนทั้งหมด (คำตอบก็ไม่ถูกบันทึก)
            plantService.changeHealth(plant.getId(), plantHealth);
        } else {
            updatePlantHealth(plant, status);
        }

        // Observer: ประกาศว่ามีการตอบรายงาน ใครฟังอยู่ก็ทำงานของตัวเองต่อ (เช่น สร้างแจ้งเตือนให้เจ้าของ)
        eventPublisher.publishEvent(new ReportResolvedEvent(
                saved.getId(), plant.getUser().getId(), plant.getId(), saved.getTitle(), status));
        return saved;
    }

    // เปลี่ยนสถานะสุขภาพต้นไม้ตามผลการตรวจ ผ่าน State pattern ของโป้ย (ห้าม set healthStatus เอง)
    //   IN_PROGRESS = admin ยืนยันว่ามีปัญหา -> SICK
    //   RESOLVED    = แก้แล้ว               -> RECOVERING
    //   PENDING / REJECTED                  -> ไม่เปลี่ยน
    // ถ้าสถานะปัจจุบันเปลี่ยนไปไม่ได้ (เช่น HEALTHY -> RECOVERING, ต้นที่ตายแล้ว) ให้ข้าม ไม่ทำให้การตอบรายงานล้ม
    private void updatePlantHealth(Plant plant, ReportStatus status) {
        HealthStatus target = switch (status) {
            case IN_PROGRESS -> HealthStatus.SICK;
            case RESOLVED -> HealthStatus.RECOVERING;
            case PENDING, REJECTED -> null;
        };
        if (target != null && PlantHealthStates.of(plant.getHealthStatus()).canChangeTo(target)) {
            plantService.changeHealth(plant.getId(), target);
        }
    }

    // อัปเดตผลได้เฉพาะรายงานของตัวเองที่ admin ตอบแล้วและยัง "กำลังดำเนินการ"
    private HealthReport findMyInProgressReport(Long id, String email) {
        HealthReport report = findMyReport(id, email);
        if (report.getStatus() != ReportStatus.IN_PROGRESS) {
            throw new InvalidRequestException("อัปเดตผลได้เฉพาะรายงานที่กำลังดำเนินการ");
        }
        return report;
    }

    // "ติดตามผล: ใบเหลือง" ติดตามซ้ำหลายรอบไม่ต้องเติมคำนำหน้าซ้ำ และไม่เกิน 150 ตัวอักษรตามคอลัมน์ title
    private String followUpTitle(String title) {
        String base = title.startsWith(FOLLOW_UP_PREFIX) ? title : FOLLOW_UP_PREFIX + title;
        return base.length() > 150 ? base.substring(0, 150) : base;
    }

    private void ensureNoOpenReport(Long plantId) {
        healthReportRepository.findFirstByPlantIdAndStatusInOrderByCreatedAtDesc(plantId, OPEN_STATUSES)
                .ifPresent(open -> {
                    throw new DuplicateReportException(
                            "ต้นนี้มีรายงานที่ยังไม่ปิดอยู่แล้ว รอผู้ดูแลระบบตอบ หรืออัปเดตผลในรายงานเดิม", open.getId());
                });
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
