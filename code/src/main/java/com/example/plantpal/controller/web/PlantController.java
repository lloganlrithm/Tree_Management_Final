package com.example.plantpal.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PlantController {

    @GetMapping("/plants")
    public String list() {
        return "plants/list";     // -> templates/plants/list.html
    }

    @GetMapping("/plants/{id}")
    public String detail(@PathVariable Long id) {
        return "plants/detail";   // -> templates/plants/detail.html
    }
}