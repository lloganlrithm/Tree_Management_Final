package com.example.plantpal.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PlantController {

    @GetMapping("/plants")
    public String list() {
        return "plants/list";   // -> templates/plants/list.html
    }
}