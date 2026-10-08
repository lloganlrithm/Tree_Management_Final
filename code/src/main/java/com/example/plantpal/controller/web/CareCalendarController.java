package com.example.plantpal.controller.web;

import com.example.plantpal.domain.entity.CareSchedule;
import com.example.plantpal.domain.entity.User;
import com.example.plantpal.dto.response.CareCalendarDay;
import com.example.plantpal.iterator.CareCalendarIterator;
import com.example.plantpal.service.CareService;
import com.example.plantpal.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// C4: ปฏิทินการดูแล 30 วันข้างหน้า (ลิงก์ "ปฏิทิน" ใน sidebar ชี้มาที่ /care/calendar)
@Controller
@RequiredArgsConstructor
public class CareCalendarController {

    private static final int CALENDAR_DAYS = 30;

    private final CareService careService;
    private final CurrentUserService currentUserService;

    @GetMapping("/care/calendar")
    public String calendar(Model model) {
        User me = currentUserService.getCurrentUser();
        LocalDate today = LocalDate.now();
        LocalDate lastDay = today.plusDays(CALENDAR_DAYS - 1L);

        // งานดูแลที่ครบกำหนดในช่วง 30 วันนี้ (ใช้ findDueBetween จาก C1)
        List<CareSchedule> schedules = careService.findDueBetween(me, today, lastDay);

        // ใช้ Iterator เดินทีละวัน แล้วเก็บเป็นรายการวันให้หน้าเว็บวนแสดง
        List<CareCalendarDay> days = new ArrayList<>();
        CareCalendarIterator iterator = new CareCalendarIterator(today, CALENDAR_DAYS, schedules);
        while (iterator.hasNext()) {
            days.add(iterator.next());
        }

        model.addAttribute("days", days);
        // ช่องว่างหน้าวันแรก ให้วันนี้ตรงกับคอลัมน์วันในสัปดาห์ (อาทิตย์ = 0 ... เสาร์
        // = 6)
        model.addAttribute("leadingBlanks", today.getDayOfWeek().getValue() % 7);
        model.addAttribute("taskCount", schedules.size());
        // งานที่เลยกำหนดไปแล้วไม่อยู่ในปฏิทิน (เป็นอดีต) แสดงเป็นแถบเตือนแทน
        model.addAttribute("overdueCount", careService.findOverdue(me).size());
        return "care/calendar";
    }
}