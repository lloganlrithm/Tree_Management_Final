package com.example.plantpal.event;

import com.example.plantpal.domain.enums.ActionType;
import com.example.plantpal.domain.enums.NotificationType;
import com.example.plantpal.domain.enums.ReportStatus;
import com.example.plantpal.service.NotificationService;
import com.example.plantpal.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// ทดสอบผู้ฟังของ Observer Pattern: ได้ event แล้วต้องสร้างแจ้งเตือนให้ถูกคน ถูกประเภท ข้อความถูก
@ExtendWith(MockitoExtension.class)
class NotificationListenerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserService userService;

    @InjectMocks
    private NotificationListener listener;

    // ---------- ReportSubmittedEvent (แจ้ง admin) ----------

    @Test
    void newReportNotifiesEveryAdmin() {
        when(userService.findAdminIds()).thenReturn(List.of(1L, 2L));

        listener.onReportSubmitted(new ReportSubmittedEvent(5L, 7L, "ฟิโลหัวใจ", "ใบเหลือง", false));

        String message = "มีรายงานใหม่: \"ใบเหลือง\" (ฟิโลหัวใจ) รอตรวจ";
        verify(notificationService).create(1L, null, NotificationType.SYSTEM, message);
        verify(notificationService).create(2L, null, NotificationType.SYSTEM, message);
    }

    @Test
    void followUpReportTellsAdminItIsNotBetter() {
        when(userService.findAdminIds()).thenReturn(List.of(1L));

        listener.onReportSubmitted(new ReportSubmittedEvent(5L, 7L, "ฟิโลหัวใจ", "ติดตามผล: ใบเหลือง", true));

        verify(notificationService).create(1L, null, NotificationType.SYSTEM,
                "ติดตามผล: \"ติดตามผล: ใบเหลือง\" (ฟิโลหัวใจ) ผู้ใช้แจ้งว่ายังไม่ดีขึ้น รอตรวจอีกรอบ");
    }

    @Test
    void adminWhoReportedDoesNotNotifyThemself() {
        // admin 1 แจ้งปัญหาต้นไม้ตัวเอง -> แจ้งแค่ admin 2
        when(userService.findAdminIds()).thenReturn(List.of(1L, 2L));

        listener.onReportSubmitted(new ReportSubmittedEvent(5L, 1L, "ฟิโลหัวใจ", "ใบเหลือง", false));

        verify(notificationService, never()).create(eq(1L), any(), any(), any());
        verify(notificationService).create(eq(2L), isNull(), eq(NotificationType.SYSTEM), anyString());
    }

    // ---------- ReportResolvedEvent ----------

    @Test
    void reportResolvedCreatesHealthReplyForOwner() {
        listener.onReportResolved(new ReportResolvedEvent(1L, 7L, 10L, "ใบเหลือง", ReportStatus.RESOLVED));

        verify(notificationService).create(7L, 10L, NotificationType.HEALTH_REPLY,
                "รายงาน \"ใบเหลือง\" แก้ไขแล้ว ดูคำแนะนำจากผู้ดูแลระบบได้เลย");
    }

    @Test
    void reportRejectedUsesClosedMessage() {
        listener.onReportResolved(new ReportResolvedEvent(1L, 7L, 10L, "ใบเหลือง", ReportStatus.REJECTED));

        verify(notificationService).create(7L, 10L, NotificationType.HEALTH_REPLY,
                "ผู้ดูแลระบบปฏิเสธรายงาน \"ใบเหลือง\" ดูเหตุผลในรายงานได้เลย");
    }

    @Test
    void autoClosedReportTellsOwnerTheyCanReportAgain() {
        listener.onReportAutoClosed(new ReportAutoClosedEvent(1L, 7L, 10L, "ใบเหลือง", 14));

        verify(notificationService).create(7L, 10L, NotificationType.SYSTEM,
                "รายงาน \"ใบเหลือง\" หมดเวลาติดตามผล เพราะไม่มีการบอกผลเกิน 14 วัน ถ้ายังมีปัญหาแจ้งใหม่ได้เลย");
    }

    // ---------- CareDueEvent ----------

    @Test
    void careDueTomorrowCreatesCareDueReminder() {
        listener.onCareDue(new CareDueEvent(7L, 10L, "ฟิโลหัวใจ", ActionType.WATER,
                LocalDate.of(2026, 10, 9), false));

        verify(notificationService).create(7L, 10L, NotificationType.CARE_DUE, "พรุ่งนี้ถึงเวลารดน้ำ ฟิโลหัวใจ");
    }

    @Test
    void overdueCareMentionsDueDate() {
        listener.onCareDue(new CareDueEvent(7L, 10L, "ยางดำ", ActionType.FERTILIZE,
                LocalDate.of(2026, 10, 6), true));

        verify(notificationService).create(7L, 10L, NotificationType.CARE_DUE,
                "เลยกำหนดใส่ปุ๋ย ยางดำ แล้ว (กำหนด 6/10/2026)");
    }
}
