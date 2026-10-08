package com.example.plantpal.job;

import com.example.plantpal.domain.entity.HealthReport;
import com.example.plantpal.service.HealthReportService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.util.List;

// Template Method (คลาสลูกที่ 3): ทุกเช้า 08:10 ปิดรายงาน "กำลังดำเนินการ" ที่ผู้ใช้ไม่ได้บอกผลเกิน 14 วัน
// ไม่ให้รายงานค้างตลอดไป ลำดับขั้นตอน / transaction / log ใช้ของ AbstractDailyJob เหมือนอีก 2 job
@Component
public class StaleReportCloseJob extends AbstractDailyJob<HealthReport> {

    // นับจากวันที่สร้างรายงานรอบนั้น (ตารางไม่มีคอลัมน์แก้ไขล่าสุด) กด "ยังไม่ดีขึ้น" = ได้รอบใหม่ นับใหม่
    public static final int STALE_DAYS = 14;

    private final HealthReportService healthReportService;

    public StaleReportCloseJob(PlatformTransactionManager transactionManager,
                               HealthReportService healthReportService) {
        super(transactionManager);
        this.healthReportService = healthReportService;
    }

    @Scheduled(cron = "0 10 8 * * *", zone = "Asia/Bangkok")
    public void scheduledRun() {
        run();
    }

    @Override
    protected String name() {
        return "StaleReportCloseJob";
    }

    // ขั้นที่ 1: รายงานกำลังดำเนินการที่สร้างก่อน (วันนี้ - 14 วัน)
    @Override
    protected List<HealthReport> findTargets(LocalDate today) {
        return healthReportService.findStaleInProgress(today.minusDays(STALE_DAYS).atStartOfDay());
    }

    // ขั้นที่ 2: ปิดรายงาน (Service จะประกาศ ReportAutoClosedEvent ให้ระบบแจ้งเตือนเจ้าของ)
    @Override
    protected void process(HealthReport report, LocalDate today) {
        healthReportService.autoClose(report, STALE_DAYS);
    }
}
