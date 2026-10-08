package com.example.plantpal.job;

import com.example.plantpal.domain.entity.CareSchedule;
import com.example.plantpal.event.CareDueEvent;
import com.example.plantpal.repository.CareScheduleRepository;
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

    // TODO: เปลี่ยนเป็น careService.findDueBetween(...) ของเปียโนเมื่อเสร็จ (ตามข้อตกลงทีม)
    // ตอนนี้ใช้ method ที่มีอยู่แล้วใน repository แบบเดียวกับ dashboard ของโป้ย
    private final CareScheduleRepository careScheduleRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CareDueReminderJob(PlatformTransactionManager transactionManager,
                              CareScheduleRepository careScheduleRepository,
                              ApplicationEventPublisher eventPublisher) {
        super(transactionManager);
        this.careScheduleRepository = careScheduleRepository;
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
        return careScheduleRepository.findByIsActiveTrueAndNextDueDateLessThanEqual(tomorrow).stream()
                .filter(s -> s.getNextDueDate().equals(tomorrow))
                .toList();
    }

    // ขั้นที่ 2: ประกาศ CareDueEvent ให้ NotificationListener สร้างแจ้งเตือน (Observer)
    @Override
    protected void process(CareSchedule schedule, LocalDate today) {
        eventPublisher.publishEvent(CareDueEvent.from(schedule, false));
    }
}
