package com.example.plantpal.controller.web;

import com.example.plantpal.dto.request.RegisterRequest;
import com.example.plantpal.exception.RegistrationException;
import com.example.plantpal.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthWebController {

    private final UserService userService;

    // E2: หน้าเข้าสู่ระบบ (POST /login ให้ Spring Security จัดการ)
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    // E3: หน้าสมัครสมาชิก
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("form", new RegisterRequest());
        return "auth/register";
    }

    // U3: บันทึกผู้ใช้ใหม่ สำเร็จแล้วกลับไปหน้า login
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegisterRequest form,
                           BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("error", result.getAllErrors().get(0).getDefaultMessage());
            return "auth/register";
        }
        try {
            userService.register(form);
        } catch (RegistrationException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
        return "redirect:/login?registered";
    }
}
