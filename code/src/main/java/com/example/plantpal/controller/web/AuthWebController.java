package com.example.plantpal.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthWebController {

    // E2: หน้าเข้าสู่ระบบ (POST /login ให้ Spring Security จัดการ)
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    // E3: หน้าสมัครสมาชิก (POST /register รอทำตอนเชื่อม UserService)
    @GetMapping("/register")
    public String register() {
        return "auth/register";
    }
}