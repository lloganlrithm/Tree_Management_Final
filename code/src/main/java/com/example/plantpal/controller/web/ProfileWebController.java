package com.example.plantpal.controller.web;

import com.example.plantpal.dto.request.ChangePasswordRequest;
import com.example.plantpal.dto.request.ProfileUpdateRequest;
import com.example.plantpal.exception.InvalidRequestException;
import com.example.plantpal.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileWebController {

    private final ProfileService profileService;

    // E4 + U4: หน้าโปรไฟล์ของฉัน
    @GetMapping
    public String profile(Model model) {
        model.addAttribute("profile", profileService.getMyProfile());
        return "profile/profile";
    }

    // บันทึกชื่อ นามสกุล เบอร์ และรูป
    @PostMapping
    public String update(@Valid @ModelAttribute ProfileUpdateRequest request, BindingResult result,
                         @RequestParam(value = "avatar", required = false) MultipartFile avatar,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            redirect.addFlashAttribute("error", result.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/profile";
        }
        try {
            profileService.updateMyProfile(request, avatar);
            redirect.addFlashAttribute("success", "บันทึกข้อมูลแล้ว");
        } catch (InvalidRequestException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/password")
    public String changePassword(@ModelAttribute ChangePasswordRequest request, RedirectAttributes redirect) {
        try {
            profileService.changeMyPassword(request);
            redirect.addFlashAttribute("success", "เปลี่ยนรหัสผ่านแล้ว");
        } catch (InvalidRequestException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile";
    }
}
