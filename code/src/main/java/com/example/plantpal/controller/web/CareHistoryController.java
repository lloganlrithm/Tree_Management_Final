package com.example.plantpal.controller.web;

import com.example.plantpal.domain.entity.CareLog;
import com.example.plantpal.domain.entity.User;
import com.example.plantpal.service.CareService;
import com.example.plantpal.service.CurrentUserService;
import com.example.plantpal.service.PlantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

// C3: หน้าประวัติการดูแล (ปุ่ม "ประวัติการดูแล" ในหน้า care ชี้มาที่ /care/history)
@Controller
@RequiredArgsConstructor
public class CareHistoryController {

    private final CareService careService;
    private final PlantService plantService;
    private final CurrentUserService currentUserService;

    // ?plantId=... ดูเฉพาะต้นไม้ 1 ต้น / ไม่ใส่ = ทุกต้น
    @GetMapping("/care/history")
    public String history(@RequestParam(required = false) Long plantId, Model model) {
        User me = currentUserService.getCurrentUser();

        List<CareLog> logs = (plantId == null)
                ? careService.findHistory(me)
                : careService.findPlantHistory(plantId, me);

        // สรุปตัวเลขด้านบน: ตรงเวลา = ทำภายในวันกำหนด (หรือก่อน), ช้า = ทำหลังวันกำหนด
        long lateCount = logs.stream().filter(CareHistoryController::isLate).count();
        long onTimeCount = logs.stream().filter(l -> l.getDueDate() != null && !isLate(l)).count();

        model.addAttribute("logs", logs);
        model.addAttribute("onTimeCount", onTimeCount);
        model.addAttribute("lateCount", lateCount);
        model.addAttribute("plants", plantService.findMyPlants(me.getEmail(), null)); // dropdown เลือกต้นไม้
        model.addAttribute("selectedPlantId", plantId);
        return "care/history";
    }

    private static boolean isLate(CareLog log) {
        return log.getDueDate() != null
                && log.getPerformedAt().toLocalDate().isAfter(log.getDueDate());
    }
}