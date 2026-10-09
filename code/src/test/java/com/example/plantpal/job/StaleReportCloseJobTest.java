package com.example.plantpal.job;

import com.example.plantpal.domain.entity.HealthReport;
import com.example.plantpal.service.HealthReportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Template Method คลาสลูกที่ 3: ใช้ลำดับขั้นตอนเดียวกับ job อื่น ต่างแค่หาอะไร / ทำอะไร
@ExtendWith(MockitoExtension.class)
class StaleReportCloseJobTest {

    @Mock private PlatformTransactionManager transactionManager;
    @Mock private HealthReportService healthReportService;

    @Test
    void closesEveryReportOlderThan14Days() {
        LocalDate today = LocalDate.now(AbstractDailyJob.ZONE);
        HealthReport first = new HealthReport();
        HealthReport second = new HealthReport();
        // ต้องถามด้วยเวลาเริ่มวันของ (วันนี้ - 14 วัน) พอดี
        when(healthReportService.findStaleInProgress(today.minusDays(14).atStartOfDay()))
                .thenReturn(List.of(first, second));
        StaleReportCloseJob job = new StaleReportCloseJob(transactionManager, healthReportService);

        int done = job.run();

        assertThat(done).isEqualTo(2);
        verify(healthReportService).autoClose(first, 14);
        verify(healthReportService).autoClose(second, 14);
    }

    @Test
    void nothingStaleClosesNothing() {
        when(healthReportService.findStaleInProgress(any())).thenReturn(List.of());
        StaleReportCloseJob job = new StaleReportCloseJob(transactionManager, healthReportService);

        assertThat(job.run()).isZero();
        verify(healthReportService, never()).autoClose(any(), anyInt());
    }
}
