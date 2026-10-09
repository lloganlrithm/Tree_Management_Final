package com.example.plantpal.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

// Template Method Pattern: โครงของงานที่รันทุกวัน
// คลาสแม่กำหนด "ลำดับขั้นตอน" ไว้ใน run() ซึ่งแก้ไม่ได้ (final)
//   1) findTargets  หาว่าวันนี้ต้องทำกับอะไรบ้าง   <- คลาสลูกเขียน
//   2) process      ทำงานกับแต่ละรายการ            <- คลาสลูกเขียน
//   3) afterRun     สรุปผลหลังทำเสร็จ (hook)        <- คลาสลูกจะเขียนทับหรือไม่ก็ได้
// คลาสลูกเขียนแค่ส่วนที่ต่างกัน ส่วนที่เหมือนกัน (transaction, วันที่, log) อยู่ที่นี่ที่เดียว
@Slf4j
public abstract class AbstractDailyJob<T> {

    // เวลาของระบบใช้เวลาไทย ไม่ว่า server จะตั้ง timezone อะไร (Railway/Render เป็น UTC)
    public static final ZoneId ZONE = ZoneId.of("Asia/Bangkok");

    private final TransactionTemplate transactionTemplate;

    protected AbstractDailyJob(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    // Template Method: final = คลาสลูก override ไม่ได้ ลำดับขั้นตอนเหมือนกันทุก job
    // ทั้งงานอยู่ใน transaction เดียว เพื่อให้อ่านข้อมูลที่เป็น LAZY (เช่น plant.user) ได้ใน process()
    public final int run() {
        LocalDate today = LocalDate.now(ZONE);
        Integer count = transactionTemplate.execute(status -> {
            List<T> targets = findTargets(today);
            targets.forEach(target -> process(target, today));
            return targets.size();
        });
        int done = (count == null) ? 0 : count;
        afterRun(done, today);
        return done;
    }

    // ชื่อ job ใช้ใน log
    protected abstract String name();

    // ขั้นที่ 1: หาสิ่งที่ต้องทำในวันนี้
    protected abstract List<T> findTargets(LocalDate today);

    // ขั้นที่ 2: ทำงานกับ 1 รายการ
    protected abstract void process(T target, LocalDate today);

    // ขั้นที่ 3 (hook): ค่าเริ่มต้นแค่เขียน log คลาสลูกเขียนทับได้ถ้าต้องการ
    protected void afterRun(int count, LocalDate today) {
        log.info("[{}] {} ทำไป {} รายการ", name(), today, count);
    }
}
