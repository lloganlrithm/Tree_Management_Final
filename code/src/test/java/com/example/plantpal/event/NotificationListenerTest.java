package com.example.plantpal.event;

import com.example.plantpal.domain.enums.ActionType;
import com.example.plantpal.domain.enums.NotificationType;
import com.example.plantpal.domain.enums.ReportStatus;
import com.example.plantpal.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.Mockito.verify;

// ทดสอบผู้ฟังของ Observer Pattern: ได้ event แล้วต้องสร้างแจ้งเตือนให้ถูกคน ถูกประเภท ข้อความถูก
@ExtendWith(MockitoExtension.class)
class NotificationListenerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationListener listener;

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
