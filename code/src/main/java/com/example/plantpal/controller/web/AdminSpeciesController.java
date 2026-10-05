package com.example.plantpal.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminSpeciesController {

    @GetMapping("/admin/species")
    public String list() {
        return "admin/species";   // -> templates/admin/species.html
    }
}