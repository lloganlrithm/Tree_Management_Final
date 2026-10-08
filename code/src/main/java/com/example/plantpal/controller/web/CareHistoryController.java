package com.example.plantpal.controller.web;

import com.example.plantpal.domain.entity.User;
import com.example.plantpal.dto.response.CareHistorySummary;
import com.example.plantpal.service.CareService;
import com.example.plantpal.service.CurrentUserService;
import com.example.plantpal.service.PlantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

// C3: หน้าประวัติการดูแล (ปุ่ม "ประวัติการดูแล" ในหน้า care ชี้มาที่ /care/history)
// Controller แค่รับ request แล้วส่งข้อมูลให้หน้าเว็บ การนับตรงเวลา/ช้าอยู่ใน CareService
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
        CareHistorySummary summary = careService.getHistorySummary(me, plantId);

        model.addAttribute("logs", summary.getLogs());
        model.addAttribute("onTimeCount", summary.getOnTimeCount());
        model.addAttribute("lateCount", summary.getLateCount());
        model.addAttribute("plants", plantService.findMyPlants(me.getEmail(), null));   // dropdown เลือกต้นไม้
        model.addAttribute("selectedPlantId", plantId);
        return "care/history";
    }
}