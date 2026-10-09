package com.example.plantpal.job;

import com.example.plantpal.domain.entity.CareSchedule;
import com.example.plantpal.event.CareDueEvent;
import com.example.plantpal.service.CareService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.util.List;

// Template Method (คลาสลูกที่ 2): ทุกเช้า 08:05 เตือนงานดูแลที่ "เลยกำหนดแล้ว" แต่ยังไม่ได้ทำ
// ต่างจาก CareDueReminderJob แค่เงื่อนไขที่หาและข้อความ (overdue = true) ส่วนขั้นตอนใช้ของคลาสแม่
@Component
public class OverdueCareReminderJob extends AbstractDailyJob<CareSchedule> {

    // เรียกผ่าน CareService ของเปียโน ไม่อ่าน repository ของโมดูลเพื่อนตรงๆ (ตามข้อตกลงทีม)
    private final CareService careService;
    private final ApplicationEventPublisher eventPublisher;

    public OverdueCareReminderJob(PlatformTransactionManager transactionManager,
                                  CareService careService,
                                  ApplicationEventPublisher eventPublisher) {
        super(transactionManager);
        this.careService = careService;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(cron = "0 5 8 * * *", zone = "Asia/Bangkok")
    public void scheduledRun() {
        run();
    }

    @Override
    protected String name() {
        return "OverdueCareReminderJob";
    }

    // ขั้นที่ 1: ตารางที่เปิดใช้อยู่และวันครบกำหนดผ่านไปแล้ว (ก่อนวันนี้)
    @Override
    protected List<CareSchedule> findTargets(LocalDate today) {
        return careService.findOverdue(today);
    }

    // ขั้นที่ 2: ประกาศ CareDueEvent แบบเลยกำหนด
    @Override
    protected void process(CareSchedule schedule, LocalDate today) {
        eventPublisher.publishEvent(CareDueEvent.from(schedule, true));
    }
}
