package com.example.plantpal.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// หน้าที่ยังไม่ต่อ Service: คืนชื่อหน้าอย่างเดียว
// (/reports ย้ายไป ReportController, /admin/reports ย้ายไป AdminReportController แล้ว)
@Controller
public class ReportViewController {

    @GetMapping("/notifications")
    public String notificationList() {
        return "notification/notification-list";
    }
}
