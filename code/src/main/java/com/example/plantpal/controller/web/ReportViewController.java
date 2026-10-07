package com.example.plantpal.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// หน้าที่ยังไม่ต่อ Service: คืนชื่อหน้าอย่างเดียว (หน้า /reports ย้ายไป ReportController แล้ว)
@Controller
public class ReportViewController {

    @GetMapping("/admin/reports")
    public String adminReportList() {
        return "admin/admin-report-list";
    }

    @GetMapping("/notifications")
    public String notificationList() {
        return "notification/notification-list";
    }
}
