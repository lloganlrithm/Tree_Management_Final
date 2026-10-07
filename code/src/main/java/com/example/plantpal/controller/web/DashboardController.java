package com.example.plantpal.controller.web;

import com.example.plantpal.service.CurrentUserService;
import com.example.plantpal.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final CurrentUserService currentUserService;

    // หน้าแรกหลัง login ของ USER (RoleBasedLoginSuccessHandler ส่งมาที่นี่)
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        String email = currentUserService.getCurrentUser().getEmail();
        LocalDate today = LocalDate.now();

        Map<String, Long> counts = dashboardService.countPlantsByStatus(email);
        model.addAttribute("counts", counts);
        model.addAttribute("totalPlants", counts.values().stream().mapToLong(Long::longValue).sum());
        model.addAttribute("tasks", dashboardService.findCareTasks(email, today.plusDays(1)));   // ถึงพรุ่งนี้
        model.addAttribute("reports", dashboardService.findPendingReports(email));
        model.addAttribute("today", today);
        return "dashboard";   // -> templates/dashboard.html
    }
}
