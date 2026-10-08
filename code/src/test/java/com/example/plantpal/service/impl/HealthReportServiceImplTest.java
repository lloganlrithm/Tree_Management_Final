package com.example.plantpal.service.impl;

import com.example.plantpal.domain.entity.HealthReport;
import com.example.plantpal.domain.entity.Plant;
import com.example.plantpal.domain.entity.User;
import com.example.plantpal.domain.enums.HealthStatus;
import com.example.plantpal.domain.enums.ReportStatus;
import com.example.plantpal.domain.enums.Severity;
import com.example.plantpal.dto.request.HealthReportRequest;
import com.example.plantpal.event.ReportResolvedEvent;
import com.example.plantpal.dto.request.ReportFollowUpRequest;
import com.example.plantpal.exception.DuplicateReportException;
import com.example.plantpal.exception.InvalidRequestException;
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

    // ---------- reply (admin): ส่งคำแนะนำ = กำลังดำเนินการ, ปฏิเสธ = ปิดพร้อมเหตุผล ----------

    @Test
    void answerMovesReportToInProgressMarksPlantSickAndPublishesEvent() {
        HealthReport report = report(plant(HealthStatus.HEALTHY), ReportStatus.PENDING);
        when(healthReportRepository.findWithPlantById(1L)).thenReturn(Optional.of(report));
        when(healthReportRepository.save(report)).thenReturn(report);

        HealthReport result = service.reply(1L, ReportStatus.IN_PROGRESS, "  ลดการรดน้ำ  ");

        assertThat(result.getStatus()).isEqualTo(ReportStatus.IN_PROGRESS);
        assertThat(result.getAdminReply()).isEqualTo("ลดการรดน้ำ");
        assertThat(result.getResolvedAt()).isNull();                     // ยังไม่ปิด รอผู้ใช้บอกผล
        verify(plantService).changeHealth(10L, HealthStatus.SICK);       // State: HEALTHY -> SICK

        // Observer: ส่ง event ไปให้ NotificationListener พร้อมข้อมูลเจ้าของต้นไม้
        ArgumentCaptor<ReportResolvedEvent> captor = ArgumentCaptor.forClass(ReportResolvedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        ReportResolvedEvent event = captor.getValue();
        assertThat(event.getOwnerId()).isEqualTo(7L);
        assertThat(event.getPlantId()).isEqualTo(10L);
        assertThat(event.getStatus()).isEqualTo(ReportStatus.IN_PROGRESS);
    }

    @Test
    void answerOnDeadPlantKeepsHealthButStillSavesReply() {
        // DEAD -> SICK เปลี่ยนไม่ได้ตาม State ของโป้ย: ข้ามการเปลี่ยนสถานะต้นไม้ ไม่ทำให้การตอบล้ม
        HealthReport report = report(plant(HealthStatus.DEAD), ReportStatus.PENDING);
        when(healthReportRepository.findWithPlantById(1L)).thenReturn(Optional.of(report));
        when(healthReportRepository.save(report)).thenReturn(report);

        HealthReport result = service.reply(1L, ReportStatus.IN_PROGRESS, "ปลูกต้นใหม่ได้เลย");

        assertThat(result.getStatus()).isEqualTo(ReportStatus.IN_PROGRESS);
        verify(plantService, never()).changeHealth(any(), any());
        verify(eventPublisher).publishEvent(any(ReportResolvedEvent.class));
    }

    @Test
    void rejectClosesReportWithReason() {
        HealthReport report = report(plant(HealthStatus.HEALTHY), ReportStatus.PENDING);
        when(healthReportRepository.findWithPlantById(1L)).thenReturn(Optional.of(report));
        when(healthReportRepository.save(report)).thenReturn(report);

        HealthReport result = service.reply(1L, ReportStatus.REJECTED, "เป็นลายใบปกติของพันธุ์นี้");

        assertThat(result.getStatus()).isEqualTo(ReportStatus.REJECTED);
        assertThat(result.getAdminReply()).isEqualTo("เป็นลายใบปกติของพันธุ์นี้");
        assertThat(result.getResolvedAt()).isNotNull();
        verify(plantService, never()).changeHealth(any(), any());   // ปฏิเสธ = ไม่แตะสถานะต้นไม้
    }

    @Test
    void rejectWithoutReasonIsRefused() {
        HealthReport report = report(plant(HealthStatus.HEALTHY), ReportStatus.PENDING);
        when(healthReportRepository.findWithPlantById(1L)).thenReturn(Optional.of(report));

        assertThatThrownBy(() -> service.reply(1L, ReportStatus.REJECTED, "   "))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("กรุณาเขียนเหตุผลที่ปฏิเสธ");
        verify(healthReportRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void answerWithoutAdviceIsRefused() {
        HealthReport report = report(plant(HealthStatus.HEALTHY), ReportStatus.PENDING);
        when(healthReportRepository.findWithPlantById(1L)).thenReturn(Optional.of(report));

        assertThatThrownBy(() -> service.reply(1L, ReportStatus.IN_PROGRESS, ""))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("กรุณาเขียนคำแนะนำ");
    }

    @Test
    void adminCannotSetResolvedDirectly() {
        // "แก้ไขแล้ว" ปิดได้โดยผู้ใช้กดดีขึ้นแล้ว หรือ job เท่านั้น
        HealthReport report = report(plant(HealthStatus.SICK), ReportStatus.IN_PROGRESS);
        when(healthReportRepository.findWithPlantById(1L)).thenReturn(Optional.of(report));

        assertThatThrownBy(() -> service.reply(1L, ReportStatus.RESOLVED, "หายแล้ว"))
                .isInstanceOf(InvalidRequestException.class);
        assertThat(report.getStatus()).isEqualTo(ReportStatus.IN_PROGRESS);
    }

    @Test
    void replyToClosedReportIsRefused() {
        HealthReport report = report(plant(HealthStatus.RECOVERING), ReportStatus.RESOLVED);
        when(healthReportRepository.findWithPlantById(1L)).thenReturn(Optional.of(report));

        assertThatThrownBy(() -> service.reply(1L, ReportStatus.IN_PROGRESS, "x"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("รายงานนี้ปิดไปแล้ว ตอบเพิ่มไม่ได้");
    }

    @Test
    void replyWithAdminChosenPlantStatusUsesItInsteadOfAutomatic() {
        // admin เลือก "ตายแล้ว" เอง: ต้องใช้ค่าที่เลือก ไม่ใช่ค่าอัตโนมัติ (ส่งคำแนะนำ -> SICK)
        HealthReport report = report(plant(HealthStatus.SICK), ReportStatus.PENDING);
        when(healthReportRepository.findWithPlantById(1L)).thenReturn(Optional.of(report));
        when(healthReportRepository.save(report)).thenReturn(report);

        service.reply(1L, ReportStatus.IN_PROGRESS, "ต้นตายแล้ว", HealthStatus.DEAD);

        verify(plantService).changeHealth(10L, HealthStatus.DEAD);
        verify(plantService, never()).changeHealth(10L, HealthStatus.SICK);
    }

    @Test
    void replyWithNotAllowedPlantStatusFailsAndPublishesNothing() {
        // State ของโป้ยไม่ยอม (เช่น DEAD -> HEALTHY): error ต้องหลุดออกไปให้ transaction ย้อน และไม่ส่งแจ้งเตือน
        HealthReport report = report(plant(HealthStatus.DEAD), ReportStatus.PENDING);
        when(healthReportRepository.findWithPlantById(1L)).thenReturn(Optional.of(report));
        when(healthReportRepository.save(report)).thenReturn(report);
        when(plantService.changeHealth(10L, HealthStatus.HEALTHY))
                .thenThrow(new InvalidHealthTransitionException(HealthStatus.DEAD, HealthStatus.HEALTHY));

        assertThatThrownBy(() -> service.reply(1L, ReportStatus.IN_PROGRESS, "x", HealthStatus.HEALTHY))
                .isInstanceOf(InvalidHealthTransitionException.class);
        verify(eventPublisher, never()).publishEvent(any());
    }

    // ---------- ผู้ใช้อัปเดตผล ----------

    @Test
    void markImprovedClosesReportAndMarksPlantRecovering() {
        HealthReport report = report(plant(HealthStatus.SICK), ReportStatus.IN_PROGRESS);
        when(healthReportRepository.findByIdAndPlantUserEmail(1L, EMAIL)).thenReturn(Optional.of(report));
        when(healthReportRepository.save(report)).thenReturn(report);

        HealthReport result = service.markImproved(1L, EMAIL);

        assertThat(result.getStatus()).isEqualTo(ReportStatus.RESOLVED);
        assertThat(result.getResolvedAt()).isNotNull();
        verify(plantService).changeHealth(10L, HealthStatus.RECOVERING);   // State: SICK -> RECOVERING
    }

    @Test
    void markImprovedOnPendingReportIsRejected() {
        // admin ยังไม่ตอบ (รอตรวจ) -> ยังอัปเดตผลไม่ได้
        HealthReport report = report(plant(HealthStatus.HEALTHY), ReportStatus.PENDING);
        when(healthReportRepository.findByIdAndPlantUserEmail(1L, EMAIL)).thenReturn(Optional.of(report));

        assertThatThrownBy(() -> service.markImproved(1L, EMAIL))
                .isInstanceOf(InvalidRequestException.class);
        verify(healthReportRepository, never()).save(any());
    }

    @Test
    void followUpClosesPreviousAndOpensNewReportWithNewImage() {
        Plant plant = plant(HealthStatus.SICK);
        HealthReport previous = report(plant, ReportStatus.IN_PROGRESS);
        previous.setSeverity(Severity.MEDIUM);
        when(healthReportRepository.findByIdAndPlantUserEmail(1L, EMAIL)).thenReturn(Optional.of(previous));
        when(healthReportRepository.save(any(HealthReport.class))).thenAnswer(inv -> inv.getArgument(0));
        MockMultipartFile image = new MockMultipartFile("image", "leaf2.png", "image/png", new byte[] {1});
        when(imageStorageService.upload(image, "reports")).thenReturn("https://res.cloudinary.com/demo/leaf2.png");
        ReportFollowUpRequest request = new ReportFollowUpRequest();
        request.setMessage("  ใบบนเริ่มเหลือง  ");
        request.setImage(image);

        HealthReport next = service.followUp(1L, request, EMAIL);

        // รอบเดิมปิด รูปเดิมยังอยู่ในแถวเดิม
        assertThat(previous.getStatus()).isEqualTo(ReportStatus.RESOLVED);
        assertThat(previous.getResolvedAt()).isNotNull();
        // รอบใหม่: ต้นเดียวกัน รอ admin ตรวจ
        assertThat(next).isNotSameAs(previous);
        assertThat(next.getPlant()).isSameAs(plant);
        assertThat(next.getStatus()).isEqualTo(ReportStatus.PENDING);
        assertThat(next.getTitle()).isEqualTo("ติดตามผล: ใบเหลือง");
        assertThat(next.getDescription()).isEqualTo("ใบบนเริ่มเหลือง");
        assertThat(next.getSeverity()).isEqualTo(Severity.MEDIUM);
        assertThat(next.getImageUrl()).isEqualTo("https://res.cloudinary.com/demo/leaf2.png");
    }

    @Test
    void followUpTwiceDoesNotRepeatPrefix() {
        HealthReport previous = report(plant(HealthStatus.SICK), ReportStatus.IN_PROGRESS);
        previous.setTitle("ติดตามผล: ใบเหลือง");
        when(healthReportRepository.findByIdAndPlantUserEmail(1L, EMAIL)).thenReturn(Optional.of(previous));
        when(healthReportRepository.save(any(HealthReport.class))).thenAnswer(inv -> inv.getArgument(0));

        HealthReport next = service.followUp(1L, new ReportFollowUpRequest(), EMAIL);

        assertThat(next.getTitle()).isEqualTo("ติดตามผล: ใบเหลือง");
        assertThat(next.getDescription()).isEqualTo("ทำตามคำแนะนำแล้วแต่ยังไม่ดีขึ้น");   // ไม่กรอกอาการ = ข้อความตั้งต้น
    }

    @Test
    void followUpWithFailedUploadKeepsPreviousOpen() {
        // อัปรูปพัง -> ต้องไม่ปิดรอบเดิม ไม่สร้างรอบใหม่ (รายงานไม่หายเฉยๆ)
        HealthReport previous = report(plant(HealthStatus.SICK), ReportStatus.IN_PROGRESS);
        when(healthReportRepository.findByIdAndPlantUserEmail(1L, EMAIL)).thenReturn(Optional.of(previous));
        MockMultipartFile svg = new MockMultipartFile("image", "x.svg", "image/svg+xml", new byte[] {1});
        when(imageStorageService.upload(svg, "reports")).thenThrow(new InvalidRequestException("รองรับเฉพาะรูป"));
        ReportFollowUpRequest request = new ReportFollowUpRequest();
        request.setImage(svg);

        assertThatThrownBy(() -> service.followUp(1L, request, EMAIL))
                .isInstanceOf(InvalidRequestException.class);
        assertThat(previous.getStatus()).isEqualTo(ReportStatus.IN_PROGRESS);
        verify(healthReportRepository, never()).save(any());
    }

    @Test
    void replyToMissingReportThrowsNotFoundAndPublishesNothing() {
        when(healthReportRepository.findWithPlantById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reply(99L, ReportStatus.IN_PROGRESS, "x"))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(eventPublisher, never()).publishEvent(any());
    }
}
