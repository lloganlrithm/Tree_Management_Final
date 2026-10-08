package com.example.plantpal.event;

import com.example.plantpal.domain.enums.ActionType;
import com.example.plantpal.domain.enums.NotificationType;
import com.example.plantpal.service.NotificationService;
import com.example.plantpal.service.UserService;
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
    // หารายชื่อ admin ผ่าน Service ของพรีม ไม่เรียก UserRepository ตรงๆ
    private final UserService userService;

    // AFTER_COMMIT: สร้างแจ้งเตือนหลังบันทึกคำตอบสำเร็จแล้วเท่านั้น ถ้าบันทึกพังจะไม่มีแจ้งเตือนหลุดไป
    // REQUIRES_NEW: transaction เดิม commit ไปแล้ว ต้องเปิดใหม่ถึงจะบันทึกแจ้งเตือนลง DB ได้
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onReportResolved(ReportResolvedEvent event) {
        String message = switch (event.getStatus()) {
            case RESOLVED -> "รายงาน \"" + event.getReportTitle() + "\" แก้ไขแล้ว ดูคำแนะนำจากผู้ดูแลระบบได้เลย";
            case REJECTED -> "ผู้ดูแลระบบปฏิเสธรายงาน \"" + event.getReportTitle() + "\" ดูเหตุผลในรายงานได้เลย";
            case IN_PROGRESS -> "ผู้ดูแลระบบส่งคำแนะนำรายงาน \"" + event.getReportTitle() + "\" แล้ว ลองทำตามแล้วบอกผลได้เลย";
            case PENDING, FOLLOWED_UP, AUTO_CLOSED -> "ผู้ดูแลระบบตอบรายงาน \"" + event.getReportTitle() + "\" แล้ว";
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

    // ผู้ฟังตัวที่ 3: job จบรายงานที่เงียบนาน -> บอกเจ้าของว่าหมดเวลาแล้ว และแจ้งใหม่ได้ถ้ายังมีปัญหา
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onReportAutoClosed(ReportAutoClosedEvent event) {
        String message = "รายงาน \"" + event.getReportTitle() + "\" หมดเวลาติดตามผล เพราะไม่มีการบอกผลเกิน "
                + event.getStaleDays() + " วัน ถ้ายังมีปัญหาแจ้งใหม่ได้เลย";
        notificationService.create(event.getOwnerId(), event.getPlantId(), NotificationType.SYSTEM, message);
    }

    // ผู้ฟังตัวที่ 4: ผู้ใช้ส่งรายงานเข้ามา -> แจ้ง admin ทุกคนที่ยังใช้งานอยู่ (ไม่แจ้งตัวเองถ้า admin เป็นคนแจ้ง)
    // plantId = null เพราะ admin เปิดหน้าต้นไม้ของคนอื่นไม่ได้ กดแจ้งเตือนแล้วไปหน้ารวมแจ้งเตือนแทน
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onReportSubmitted(ReportSubmittedEvent event) {
        String message = event.isFollowUp()
                ? "ติดตามผล: \"" + event.getReportTitle() + "\" (" + event.getPlantName() + ") ผู้ใช้แจ้งว่ายังไม่ดีขึ้น รอตรวจอีกรอบ"
                : "มีรายงานใหม่: \"" + event.getReportTitle() + "\" (" + event.getPlantName() + ") รอตรวจ";
        for (Long adminId : userService.findAdminIds()) {
            if (!adminId.equals(event.getOwnerId())) {
                notificationService.create(adminId, null, NotificationType.SYSTEM, message);
            }
        }
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
