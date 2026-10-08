package com.example.plantpal.controller.web;

import com.example.plantpal.domain.entity.Notification;
import com.example.plantpal.service.CurrentUserService;
import com.example.plantpal.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

// ส่งข้อมูลกระดิ่งแจ้งเตือนให้ทุกหน้าอัตโนมัติ (fragments/notification-bell.html ใช้)
// แยกจาก GlobalWebModelAdvice จะได้ไม่ต้องแก้ไฟล์เพื่อน
@ControllerAdvice(basePackages = "com.example.plantpal.controller.web")
@RequiredArgsConstructor
public class NotificationModelAdvice {

    private static final int BELL_SIZE = 5;

    private final NotificationService notificationService;
    private final CurrentUserService currentUserService;

    @ModelAttribute
    public void addBell(Authentication authentication, Model model) {
        // หน้าก่อน login (หน้าแรก / login / สมัคร) ไม่มีกระดิ่ง
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            model.addAttribute("unreadCount", 0L);
            model.addAttribute("latestNotifications", List.<Notification>of());
            return;
        }
        Long userId = currentUserService.getCurrentUserId();
        model.addAttribute("unreadCount", notificationService.countUnread(userId));
        model.addAttribute("latestNotifications", notificationService.findLatest(userId, BELL_SIZE));
    }
}
