package com.example.plantpal.service.impl;

import com.example.plantpal.domain.entity.HealthReport;
import com.example.plantpal.domain.entity.Plant;
import com.example.plantpal.domain.entity.User;
import com.example.plantpal.domain.enums.HealthStatus;
import com.example.plantpal.domain.enums.ReportStatus;
import com.example.plantpal.domain.enums.Severity;
import com.example.plantpal.dto.request.HealthReportRequest;
import com.example.plantpal.event.ReportResolvedEvent;
import com.example.plantpal.exception.DuplicateReportException;
import com.example.plantpal.exception.ResourceNotFoundException;
import com.example.plantpal.plant.state.InvalidHealthTransitionException;
import com.example.plantpal.repository.HealthReportRepository;
import com.example.plantpal.service.ImageStorageService;
import com.example.plantpal.service.PlantService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// ทดสอบ HealthReportServiceImpl อย่างเดียว ของที่มันเรียก (DB, PlantService, Cloudinary, event) เป็นตัวปลอมทั้งหมด
@ExtendWith(MockitoExtension.class)
class HealthReportServiceImplTest {

    private static final String EMAIL = "user@plantpal.com";

    @Mock private HealthReportRepository healthReportRepository;
    @Mock private PlantService plantService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private ImageStorageService imageStorageService;

    @InjectMocks
    private HealthReportServiceImpl service;

    // ต้นไม้ id 10 ของผู้ใช้ id 7
    private Plant plant(HealthStatus status) {
        Plant plant = new Plant();
        plant.setId(10L);
        plant.setUser(User.builder().id(7L).email(EMAIL).build());
        plant.setHealthStatus(status);
        return plant;
    }

    private HealthReport report(Plant plant, ReportStatus status) {
        HealthReport report = new HealthReport();
        report.setId(1L);
        report.setPlant(plant);
        report.setTitle("ใบเหลือง");
        report.setStatus(status);
        return report;
    }

    private HealthReportRequest request(String title, String description) {
        HealthReportRequest request = new HealthReportRequest();
        request.setPlantId(10L);
        request.setTitle(title);
        request.setDescription(description);
        request.setSeverity(Severity.HIGH);
        return request;
    }

    // ---------- create ----------

    @Test
    void createSavesPendingReportWithTrimmedText() {
        when(plantService.findMyPlant(10L, EMAIL)).thenReturn(plant(HealthStatus.HEALTHY));
        when(healthReportRepository.save(any(HealthReport.class))).thenAnswer(inv -> inv.getArgument(0));

        HealthReport result = service.create(request("  ใบเหลือง  ", "   "), EMAIL);

        assertThat(result.getStatus()).isEqualTo(ReportStatus.PENDING);
        assertThat(result.getTitle()).isEqualTo("ใบเหลือง");       // ตัดช่องว่างหัวท้าย
        assertThat(result.getDescription()).isNull();              // กรอกแต่ช่องว่าง = ไม่มีรายละเอียด
        assertThat(result.getSeverity()).isEqualTo(Severity.HIGH);
        assertThat(result.getImageUrl()).isNull();
        verify(imageStorageService, never()).upload(any(), anyString());   // ไม่แนบรูป = ไม่อัปโหลด
    }

    @Test
    void createWithImageStoresUploadedUrl() {
        when(plantService.findMyPlant(10L, EMAIL)).thenReturn(plant(HealthStatus.HEALTHY));
        when(healthReportRepository.save(any(HealthReport.class))).thenAnswer(inv -> inv.getArgument(0));
        MockMultipartFile image = new MockMultipartFile("image", "leaf.png", "image/png", new byte[] {1, 2, 3});
        when(imageStorageService.upload(image, "reports")).thenReturn("https://res.cloudinary.com/demo/leaf.png");
        HealthReportRequest request = request("ใบเหลือง", null);
        request.setImage(image);

        HealthReport result = service.create(request, EMAIL);

        assertThat(result.getImageUrl()).isEqualTo("https://res.cloudinary.com/demo/leaf.png");
    }

    @Test
    void createWhenPlantHasOpenReportThrowsConflictAndUploadsNothing() {
        // ต้นนี้มีรายงาน "รอตรวจ" อยู่แล้ว -> แจ้งซ้ำไม่ได้ และไม่อัปรูปทิ้งเปล่าๆ
        when(plantService.findMyPlant(10L, EMAIL)).thenReturn(plant(HealthStatus.SICK));
        HealthReport open = report(plant(HealthStatus.SICK), ReportStatus.PENDING);
        open.setId(5L);
        when(healthReportRepository.findFirstByPlantIdAndStatusInOrderByCreatedAtDesc(any(), any()))
                .thenReturn(Optional.of(open));
        HealthReportRequest request = request("ใบเหลืองอีกแล้ว", null);
        request.setImage(new MockMultipartFile("image", "leaf.png", "image/png", new byte[] {1}));

        assertThatThrownBy(() -> service.create(request, EMAIL))
                .isInstanceOf(DuplicateReportException.class)
                .extracting("openReportId").isEqualTo(5L);   // หน้าเว็บใช้ทำลิงก์ไปรายงานเดิม
        verify(imageStorageService, never()).upload(any(), anyString());
        verify(healthReportRepository, never()).save(any());
    }

    // ---------- findMyReport ----------

    @Test
    void findMyReportOfOtherUserThrowsNotFound() {
        // repository หาด้วย id + อีเมลเจ้าของ ถ้าไม่ใช่ของเราจะได้ค่าว่าง
        when(healthReportRepository.findByIdAndPlantUserEmail(1L, EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findMyReport(1L, EMAIL))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("ไม่พบรายงานนี้");
    }

    // ---------- reply ----------

    @Test
    void replyResolvedClosesReportMarksPlantRecoveringAndPublishesEvent() {
        HealthReport report = report(plant(HealthStatus.SICK), ReportStatus.IN_PROGRESS);
        when(healthReportRepository.findWithPlantById(1L)).thenReturn(Optional.of(report));
        when(healthReportRepository.save(report)).thenReturn(report);

        HealthReport result = service.reply(1L, ReportStatus.RESOLVED, "  ลดการรดน้ำ  ");

        assertThat(result.getStatus()).isEqualTo(ReportStatus.RESOLVED);
        assertThat(result.getAdminReply()).isEqualTo("ลดการรดน้ำ");
        assertThat(result.getResolvedAt()).isNotNull();                          // ปิดเรื่องแล้วมีเวลาปิด
        verify(plantService).changeHealth(10L, HealthStatus.RECOVERING);         // State: SICK -> RECOVERING

        // Observer: ส่ง event ไปให้ NotificationListener พร้อมข้อมูลเจ้าของต้นไม้
        ArgumentCaptor<ReportResolvedEvent> captor = ArgumentCaptor.forClass(ReportResolvedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        ReportResolvedEvent event = captor.getValue();
        assertThat(event.getOwnerId()).isEqualTo(7L);
        assertThat(event.getPlantId()).isEqualTo(10L);
        assertThat(event.getStatus()).isEqualTo(ReportStatus.RESOLVED);
    }

    @Test
    void replyResolvedOnHealthyPlantKeepsHealthButStillSavesReply() {
        // HEALTHY -> RECOVERING เปลี่ยนไม่ได้ตาม State ของโป้ย: ต้องข้าม ไม่ทำให้การตอบล้ม
        HealthReport report = report(plant(HealthStatus.HEALTHY), ReportStatus.PENDING);
        when(healthReportRepository.findWithPlantById(1L)).thenReturn(Optional.of(report));
        when(healthReportRepository.save(report)).thenReturn(report);

        HealthReport result = service.reply(1L, ReportStatus.RESOLVED, "ไม่มีอะไรน่าห่วง");

        assertThat(result.getStatus()).isEqualTo(ReportStatus.RESOLVED);
        verify(plantService, never()).changeHealth(any(), any());
        verify(eventPublisher).publishEvent(any(ReportResolvedEvent.class));
    }

    @Test
    void replyInProgressReopensReportAndMarksPlantSick() {
        HealthReport report = report(plant(HealthStatus.HEALTHY), ReportStatus.RESOLVED);
        report.setResolvedAt(LocalDateTime.now().minusDays(1));
        when(healthReportRepository.findWithPlantById(1L)).thenReturn(Optional.of(report));
        when(healthReportRepository.save(report)).thenReturn(report);

        HealthReport result = service.reply(1L, ReportStatus.IN_PROGRESS, "");

        assertThat(result.getResolvedAt()).isNull();         // เปิดเรื่องกลับมา = ล้างเวลาปิด
        assertThat(result.getAdminReply()).isNull();          // คำตอบว่าง = ไม่มีคำตอบ
        verify(plantService).changeHealth(10L, HealthStatus.SICK);
    }

    @Test
    void replyWithAdminChosenPlantStatusUsesItInsteadOfAutomatic() {
        // admin เลือก "ตายแล้ว" เอง: ต้องใช้ค่าที่เลือก ไม่ใช่ค่าอัตโนมัติ (RESOLVED -> RECOVERING)
        HealthReport report = report(plant(HealthStatus.SICK), ReportStatus.IN_PROGRESS);
        when(healthReportRepository.findWithPlantById(1L)).thenReturn(Optional.of(report));
        when(healthReportRepository.save(report)).thenReturn(report);

        service.reply(1L, ReportStatus.RESOLVED, "ต้นตายแล้ว", HealthStatus.DEAD);

        verify(plantService).changeHealth(10L, HealthStatus.DEAD);
        verify(plantService, never()).changeHealth(10L, HealthStatus.RECOVERING);
    }

    @Test
    void replyWithNotAllowedPlantStatusFailsAndPublishesNothing() {
        // State ของโป้ยไม่ยอม (เช่น DEAD -> HEALTHY): error ต้องหลุดออกไปให้ transaction ย้อน และไม่ส่งแจ้งเตือน
        HealthReport report = report(plant(HealthStatus.DEAD), ReportStatus.PENDING);
        when(healthReportRepository.findWithPlantById(1L)).thenReturn(Optional.of(report));
        when(healthReportRepository.save(report)).thenReturn(report);
        when(plantService.changeHealth(10L, HealthStatus.HEALTHY))
                .thenThrow(new InvalidHealthTransitionException(HealthStatus.DEAD, HealthStatus.HEALTHY));

        assertThatThrownBy(() -> service.reply(1L, ReportStatus.RESOLVED, "x", HealthStatus.HEALTHY))
                .isInstanceOf(InvalidHealthTransitionException.class);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void replyToMissingReportThrowsNotFoundAndPublishesNothing() {
        when(healthReportRepository.findWithPlantById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reply(99L, ReportStatus.RESOLVED, "x"))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(eventPublisher, never()).publishEvent(any());
    }
}
