package com.example.plantpal.controller.web;

import com.example.plantpal.domain.enums.ReportStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// ขั้นสุดท้ายของ "ความคืบหน้า" ต้องขึ้นตามผลจริง ไม่ใช่ "ปิดเรื่อง" เหมือนกันทุกแบบ
class ReportLabelsTest {

    private final ReportLabels labels = new ReportLabels();

    @Test
    void openReportWaitsForResult() {
        assertThat(labels.outcome(ReportStatus.PENDING)).isEqualTo("รอผลการดูแล");
        assertThat(labels.outcome(ReportStatus.IN_PROGRESS)).isEqualTo("รอผลการดูแล");
        assertThat(labels.closed(ReportStatus.PENDING)).isFalse();
        assertThat(labels.closed(ReportStatus.IN_PROGRESS)).isFalse();
    }

    @Test
    void finishedReportShowsHowItEnded() {
        assertThat(labels.outcome(ReportStatus.RESOLVED)).isEqualTo("ต้นไม้ดีขึ้นแล้ว");
        assertThat(labels.outcome(ReportStatus.FOLLOWED_UP)).isEqualTo("ส่งต่อรอบใหม่");
        assertThat(labels.outcome(ReportStatus.AUTO_CLOSED)).isEqualTo("หมดเวลาติดตามผล");
        assertThat(labels.outcome(ReportStatus.REJECTED)).isEqualTo("ผู้ดูแลระบบปฏิเสธ");
    }

    @Test
    void everyFinishedStatusCountsAsClosed() {
        assertThat(labels.closed(ReportStatus.RESOLVED)).isTrue();
        assertThat(labels.closed(ReportStatus.REJECTED)).isTrue();
        assertThat(labels.closed(ReportStatus.FOLLOWED_UP)).isTrue();
        assertThat(labels.closed(ReportStatus.AUTO_CLOSED)).isTrue();
    }
}
