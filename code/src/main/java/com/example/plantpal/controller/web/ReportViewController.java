package com.example.plantpal.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// หน้ารายงานสุขภาพและแจ้งเตือน: ตอนนี้คืนชื่อหน้าอย่างเดียว ยังไม่ต่อ Service
@Controller
public class ReportViewController {

    @GetMapping("/reports")
    public String reportList() {
        return "reports/report-list"; // -> templates/reports/report-list.html
    }

    @GetMapping("/reports/new")
    public String reportForm() {
        return "reports/report-form";
    }

    @GetMapping("/reports/{id}")
    public String reportDetail() {
        return "reports/report-detail";
    }

    @GetMapping("/admin/reports")
    public String adminReportList() {
        return "admin/admin-report-list";
    }

    @GetMapping("/notifications")
    public String notificationList() {
        return "notification/notification-list";
    }
}
