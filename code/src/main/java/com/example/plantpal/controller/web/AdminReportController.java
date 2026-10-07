package com.example.plantpal.controller.web;

import com.example.plantpal.domain.enums.ReportStatus;
import com.example.plantpal.domain.enums.Severity;
import com.example.plantpal.service.HealthReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// หน้า Admin ตรวจและตอบรายงานสุขภาพ (SecurityConfig จำกัด /admin/** ให้เฉพาะ ADMIN แล้ว)
@Controller
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private static final int PAGE_SIZE = 10;

    private final HealthReportService healthReportService;

    // ตารางรายงานทั้งหมด กรองด้วย ?status=...&severity=... แบ่งหน้าด้วย ?page=0
    @GetMapping
    public String list(@RequestParam(required = false) ReportStatus status,
                       @RequestParam(required = false) Severity severity,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        model.addAttribute("reports", healthReportService.findAll(status, severity, pageable));
        model.addAttribute("status", status);
        model.addAttribute("severity", severity);
        return "admin/admin-report-list";
    }

    // แผงตอบรายงาน: เปลี่ยนสถานะ + เขียนคำตอบ แล้วกลับไปหน้าเดิมพร้อมตัวกรองเดิม
    @PostMapping("/reply")
    public String reply(@RequestParam Long id,
                        @RequestParam ReportStatus status,
                        @RequestParam(required = false) String adminReply,
                        @RequestParam(required = false) String back,
                        RedirectAttributes redirect) {
        try {
            healthReportService.reply(id, status, adminReply);
            redirect.addFlashAttribute("success", "บันทึกคำตอบแล้ว ระบบแจ้งเตือนเจ้าของต้นไม้ให้แล้ว");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        // back = query string ของตัวกรองเดิม เช่น status=PENDING&page=1
        // รับเฉพาะตัวอักษร ตัวเลข _ = & กันคนแอบใส่ URL เว็บอื่นให้ redirect ออกไป
        boolean safeBack = back != null && back.matches("[A-Za-z0-9_=&]*");
        return "redirect:/admin/reports" + (safeBack && !back.isEmpty() ? "?" + back : "");
    }
}
