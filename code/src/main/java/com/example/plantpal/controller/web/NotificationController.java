package com.example.plantpal.controller.web;

import com.example.plantpal.service.CurrentUserService;
import com.example.plantpal.service.NotificationService;
import com.example.plantpal.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// หน้าแจ้งเตือนของผู้ใช้ที่ login อยู่
@Controller
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private static final int PAGE_SIZE = 20;

    private final NotificationService notificationService;
    private final CurrentUserService currentUserService;

    // ?unread=true = เฉพาะที่ยังไม่อ่าน, ?page=0 = แบ่งหน้า
    @GetMapping
    public String list(@RequestParam(defaultValue = "false") boolean unread,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        Long userId = currentUserService.getCurrentUserId();
        Pageable pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        model.addAttribute("notifications", notificationService.findMine(userId, unread, pageable));
        model.addAttribute("unread", unread);
        return "notification/notification-list";
    }

    @PostMapping("/{id}/read")
    public String markAsRead(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            notificationService.markAsRead(id, currentUserService.getCurrentUserId());
        } catch (IllegalArgumentException | ResourceNotFoundException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/notifications";
    }

    @PostMapping("/read-all")
    public String markAllAsRead(RedirectAttributes redirect) {
        int count = notificationService.markAllAsRead(currentUserService.getCurrentUserId());
        redirect.addFlashAttribute("success", count > 0 ? "อ่านแล้ว " + count + " รายการ" : "ไม่มีรายการที่ยังไม่อ่าน");
        return "redirect:/notifications";
    }
}
