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

// Template Method (คลาสลูกที่ 1): ทุกเช้า 08:00 เตือนงานดูแลที่ "ครบกำหนดพรุ่งนี้"
// เขียนแค่ 2 ขั้นที่ต่าง (หาอะไร / ทำอะไร) ลำดับขั้นตอนกับ transaction มาจาก AbstractDailyJob
@Component
public class CareDueReminderJob extends AbstractDailyJob<CareSchedule> {

    // เรียกผ่าน CareService ของเปียโน ไม่อ่าน repository ของโมดูลเพื่อนตรงๆ (ตามข้อตกลงทีม)
    private final CareService careService;
    private final ApplicationEventPublisher eventPublisher;

    public CareDueReminderJob(PlatformTransactionManager transactionManager,
                              CareService careService,
                              ApplicationEventPublisher eventPublisher) {
        super(transactionManager);
        this.careService = careService;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Bangkok")
    public void scheduledRun() {
        run();
    }

    @Override
    protected String name() {
        return "CareDueReminderJob";
    }

    // ขั้นที่ 1: ตารางที่เปิดใช้อยู่และครบกำหนดพรุ่งนี้พอดี
    @Override
    protected List<CareSchedule> findTargets(LocalDate today) {
        LocalDate tomorrow = today.plusDays(1);
        return careService.findDueBetween(tomorrow, tomorrow);
    }

    // ขั้นที่ 2: ประกาศ CareDueEvent ให้ NotificationListener สร้างแจ้งเตือน (Observer)
    @Override
    protected void process(CareSchedule schedule, LocalDate today) {
        eventPublisher.publishEvent(CareDueEvent.from(schedule, false));
    }
}
