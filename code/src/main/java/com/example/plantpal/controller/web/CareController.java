package com.example.plantpal.controller.web;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.plantpal.domain.entity.CareSchedule;
import com.example.plantpal.repository.CareScheduleRepository;

@Controller
public class CareController {

    private final CareScheduleRepository careScheduleRepository;

    public CareController(CareScheduleRepository careScheduleRepository) {
        this.careScheduleRepository = careScheduleRepository;
    }

    @GetMapping("/care")
    public String care(Model model) {

        List<CareSchedule> schedules = careScheduleRepository.findAll();

        model.addAttribute("schedules", schedules);

        return "care/care";
    }
}