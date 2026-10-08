package com.example.plantpal.event;

import com.example.plantpal.domain.enums.ActionType;
import com.example.plantpal.domain.enums.NotificationType;
import com.example.plantpal.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.format.DateTimeFormatter;

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
            case REJECTED -> "ผู้ดูแลระบบปฏิเสธรายงาน \"" + event.getReportTitle() + "\" ดูเหตุผลในรายงานได้เลย";
            case IN_PROGRESS -> "ผู้ดูแลระบบส่งคำแนะนำรายงาน \"" + event.getReportTitle() + "\" แล้ว ลองทำตามแล้วบอกผลได้เลย";
            case PENDING -> "ผู้ดูแลระบบตอบรายงาน \"" + event.getReportTitle() + "\" แล้ว";
        };
        notificationService.create(event.getOwnerId(), event.getPlantId(), NotificationType.HEALTH_REPLY, message);
    }

    // ผู้ฟังตัวที่ 2: เพิ่ม method ใหม่ ไม่ต้องแก้ของเดิม (Open/Closed)
    // Job รายวันทำงานใน transaction เลยใช้ AFTER_COMMIT + REQUIRES_NEW แบบเดียวกับข้างบน
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onCareDue(CareDueEvent event) {
        String action = actionLabel(event.getActionType());
        String date = event.getDueDate().format(DateTimeFormatter.ofPattern("d/M/yyyy"));
        String message = event.isOverdue()
                ? "เลยกำหนด" + action + " " + event.getPlantName() + " แล้ว (กำหนด " + date + ")"
                : "พรุ่งนี้ถึงเวลา" + action + " " + event.getPlantName();
        notificationService.create(event.getOwnerId(), event.getPlantId(), NotificationType.CARE_DUE, message);
    }

    private String actionLabel(ActionType type) {
        return switch (type) {
            case WATER -> "รดน้ำ";
            case FERTILIZE -> "ใส่ปุ๋ย";
            case REPOT -> "เปลี่ยนกระถาง";
            case CHECK_SUNLIGHT -> "ตรวจแสงแดด";
            case HEALTH_CHECK -> "ตรวจสุขภาพ";
        };
    }
}
