package com.example.plantpal.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileWebController {

    // E4: หน้าโปรไฟล์ของฉัน
    @GetMapping("/profile")
    public String profile() {
        return "profile/profile";
    }
}