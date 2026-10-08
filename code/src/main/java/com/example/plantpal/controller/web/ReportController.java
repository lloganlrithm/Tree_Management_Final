package com.example.plantpal.controller.web;

import com.example.plantpal.domain.entity.HealthReport;
import com.example.plantpal.domain.enums.ReportStatus;
import com.example.plantpal.dto.request.HealthReportRequest;
import com.example.plantpal.service.CurrentUserService;
import com.example.plantpal.service.HealthReportService;
import com.example.plantpal.service.PlantService;
import jakarta.validation.Valid;
import com.example.plantpal.exception.DuplicateReportException;
import com.example.plantpal.exception.InvalidRequestException;
import com.example.plantpal.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// หน้ารายงานสุขภาพฝั่งผู้ใช้: แจ้งปัญหา / รายงานของฉัน / รายละเอียดรายงาน
@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private static final int PAGE_SIZE = 10;

    private final HealthReportService healthReportService;
    private final PlantService plantService;
    private final CurrentUserService currentUserService;

    // รายงานของฉัน (+ กรองด้วย ?status=PENDING และแบ่งหน้าด้วย ?page=0)
    @GetMapping
    public String list(@RequestParam(required = false) ReportStatus status,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        model.addAttribute("reports", healthReportService.findMyReports(currentEmail(), status, pageable));
        model.addAttribute("status", status);   // ให้ chip ตัวกรองรู้ว่าเลือกอันไหนอยู่
        return "reports/report-list";
    }

    // ฟอร์มแจ้งปัญหา มาจากหน้ารายละเอียดต้นไม้จะมี ?plantId=... ให้เลือกต้นนั้นไว้ก่อน
    @GetMapping("/new")
    public String form(@RequestParam(required = false) Long plantId, Model model) {
        model.addAttribute("plants", plantService.findMyPlants(currentEmail(), null));
        model.addAttribute("plantId", plantId);
        return "reports/report-form";
    }

    // ส่งไม่ผ่าน = แสดงฟอร์มเดิมพร้อมข้อความที่พิมพ์ไว้ (ไม่ redirect เพราะ redirect จะทำให้ข้อมูลในฟอร์มหาย)
    @PostMapping
    public String create(@Valid @ModelAttribute("form") HealthReportRequest request,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return showFormAgain(request, result.getAllErrors().get(0).getDefaultMessage(), model);
        }
        try {
            HealthReport report = healthReportService.create(request, currentEmail());
            redirect.addFlashAttribute("success", "ส่งรายงานแล้ว ผู้ดูแลระบบจะตอบกลับเร็วๆ นี้");
            return "redirect:/reports/" + report.getId();
        } catch (DuplicateReportException e) {
            model.addAttribute("openReportId", e.getOpenReportId());   // ทำลิงก์ไปรายงานเดิมในกล่องแดง
            return showFormAgain(request, e.getMessage(), model);
        } catch (IllegalArgumentException | ResourceNotFoundException | InvalidRequestException | IllegalStateException e) {
            return showFormAgain(request, e.getMessage(), model);
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        try {
            model.addAttribute("report", healthReportService.findMyReport(id, currentEmail()));
            return "reports/report-detail";
        } catch (IllegalArgumentException | ResourceNotFoundException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/reports";
        }
    }

    // กรอกผิดแล้วแสดงฟอร์มเดิม: ค่าที่พิมพ์ไว้อยู่ใน "form" (หัวข้อ รายละเอียด ความรุนแรง ต้นไม้)
    // ส่วนไฟล์รูปเบราว์เซอร์ไม่ยอมให้เติมกลับ ผู้ใช้ต้องเลือกรูปใหม่
    private String showFormAgain(HealthReportRequest request, String message, Model model) {
        model.addAttribute("error", message);
        model.addAttribute("imageLost", request.getImage() != null && !request.getImage().isEmpty());
        model.addAttribute("plants", plantService.findMyPlants(currentEmail(), null));
        model.addAttribute("plantId", request.getPlantId());
        return "reports/report-form";
    }

    // อีเมลของคนที่ login อยู่ ถามผ่าน CurrentUserService ของพรีม (ตามกติกาทีม)
    private String currentEmail() {
        return currentUserService.getCurrentUser().getEmail();
    }
}
