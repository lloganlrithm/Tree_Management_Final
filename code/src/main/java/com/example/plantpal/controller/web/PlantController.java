package com.example.plantpal.controller.web;

import com.example.plantpal.dto.request.PlantRequest;
import com.example.plantpal.service.PlantService;
import com.example.plantpal.service.SpeciesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/plants")
@RequiredArgsConstructor
public class PlantController {

    private final PlantService plantService;
    private final SpeciesService speciesService;

    // หน้ารายการต้นไม้ของฉัน (+ ค้นหาด้วย ?keyword=...)
    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       Principal principal, Model model) {
        model.addAttribute("plants", plantService.findMyPlants(currentEmail(principal), keyword));
        model.addAttribute("speciesList", speciesService.findAll());   // ใช้ใน dropdown ของฟอร์มเพิ่มต้นไม้
        model.addAttribute("keyword", keyword);                        // ให้ช่องค้นหาจำคำที่พิมพ์ไว้
        return "plants/list";
    }

    // หน้ารายละเอียดต้นไม้ 1 ต้น
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Principal principal,
                         Model model, RedirectAttributes redirect) {
        try {
            model.addAttribute("plant", plantService.findMyPlant(id, currentEmail(principal)));
            model.addAttribute("speciesList", speciesService.findAll());   // ใช้ใน dropdown ของฟอร์มแก้ไข
            return "plants/detail";
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/plants";
        }
    }

    // ฟอร์มเพิ่ม/แก้ไขส่งมาที่นี่ (id ว่าง = เพิ่ม, มี id = แก้ไข)
    @PostMapping
    public String save(@Valid @ModelAttribute PlantRequest request,
                       BindingResult result,
                       Principal principal,
                       RedirectAttributes redirect) {
        // แก้ไขเสร็จกลับไปหน้า detail, เพิ่มใหม่กลับไปหน้า list
        String back = (request.getId() == null) ? "redirect:/plants" : "redirect:/plants/" + request.getId();

        if (result.hasErrors()) {
            redirect.addFlashAttribute("error", result.getAllErrors().get(0).getDefaultMessage());
            return back;
        }
        try {
            if (request.getId() == null) {
                plantService.create(request, currentEmail(principal));
                redirect.addFlashAttribute("success", "เพิ่มต้นไม้แล้ว");
            } else {
                plantService.update(request, currentEmail(principal));
                redirect.addFlashAttribute("success", "แก้ไขข้อมูลต้นไม้แล้ว");
            }
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return back;
    }

    @PostMapping("/delete")
    public String delete(@RequestParam Long id, Principal principal, RedirectAttributes redirect) {
        try {
            plantService.delete(id, currentEmail(principal));
            redirect.addFlashAttribute("success", "ลบต้นไม้แล้ว");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/plants";
    }

    // อีเมลของคนที่ล็อกอินอยู่
    // TODO: ถ้าทีมใช้ email เป็น username ตอน login (CustomUserDetailsService) ตรงนี้ใช้ได้เลย
    private String currentEmail(Principal principal) {
        return principal.getName();
    }
}