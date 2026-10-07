package com.example.plantpal.controller.web;

import com.example.plantpal.domain.entity.User;
import com.example.plantpal.service.CareService;
import com.example.plantpal.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/care")
@RequiredArgsConstructor
public class CareController {

    private final CareService careService;
    private final CurrentUserService currentUserService;   // คนที่ login อยู่ (ของพรีม)

    // หน้าตารางดูแล: ต้นไม้ทุกต้นของคนที่ login แบ่งเป็น เลยกำหนด / วันนี้ / ถัดไป
    @GetMapping
    public String care(Model model) {
        User me = currentUserService.getCurrentUser();
        model.addAttribute("overdue", careService.findOverdue(me));
        model.addAttribute("dueToday", careService.findDueToday(me));
        model.addAttribute("upcoming", careService.findUpcoming(me));
        return "care/care";
    }

    // ปุ่ม "ทำแล้ว" ในการ์ด -> บันทึก care_logs + เลื่อนวันกำหนดรอบถัดไป
    @PostMapping("/done")
    public String done(@RequestParam Long scheduleId,
                       @RequestParam(required = false) String notes,
                       RedirectAttributes redirect) {
        try {
            careService.markDone(scheduleId, currentUserService.getCurrentUser(), notes);
            redirect.addFlashAttribute("success", "บันทึกการดูแลแล้ว");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/care";
    }
}