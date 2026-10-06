package com.example.plantpal.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/users")
public class AdminUserWebController {

    // E5: Admin จัดการผู้ใช้
    @GetMapping
    public String list() {
        return "admin/users";
    }
}