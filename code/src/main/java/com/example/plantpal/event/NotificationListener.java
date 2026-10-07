package com.example.plantpal.event;

import com.example.plantpal.domain.enums.NotificationType;
import com.example.plantpal.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

// Observer Pattern: ผู้ฟัง รับ event จากโมดูลอื่นแล้วสร้างแจ้งเตือน
// โมดูลที่ส่ง event ไม่ต้องรู้จัก NotificationService เลย
@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationService notificationService;

    // AFTER_COMMIT: สร้างแจ้งเตือนหลังบันทึกคำตอบสำเร็จแล้วเท่านั้น ถ้าบันทึกพังจะไม่มีแจ้งเตือนหลุดไป
    // REQUIRES_NEW: transaction เดิม commit ไปแล้ว ต้องเปิดใหม่ถึงจะบันทึกแจ้งเตือนลง DB ได้
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onReportResolved(ReportResolvedEvent event) {
        String message = switch (event.getStatus()) {
            case RESOLVED -> "รายงาน \"" + event.getReportTitle() + "\" แก้ไขแล้ว ดูคำแนะนำจากผู้ดูแลระบบได้เลย";
            case REJECTED -> "ผู้ดูแลระบบปิดรายงาน \"" + event.getReportTitle() + "\" แล้ว";
            case IN_PROGRESS -> "ผู้ดูแลระบบกำลังดูรายงาน \"" + event.getReportTitle() + "\" และตอบกลับแล้ว";
            case PENDING -> "ผู้ดูแลระบบตอบรายงาน \"" + event.getReportTitle() + "\" แล้ว";
        };
        notificationService.create(event.getOwnerId(), event.getPlantId(), NotificationType.HEALTH_REPLY, message);
    }
}
