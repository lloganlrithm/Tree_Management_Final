package com.example.plantpal.controller.web;

import com.example.plantpal.dto.request.SpeciesRequest;
import com.example.plantpal.service.SpeciesService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/species")
@RequiredArgsConstructor
public class AdminSpeciesController {

    private final SpeciesService speciesService;

    // แสดงรายการพันธุ์ไม้ทั้งหมดจาก DB
    @GetMapping
    public String list(Model model) {
        model.addAttribute("speciesList", speciesService.findAll());
        return "admin/species";   // -> templates/admin/species.html
    }

    // ฟอร์มเพิ่ม/แก้ไข ส่งมาที่นี่ (id ว่าง = เพิ่ม, มี id = แก้ไข)
    @PostMapping
    public String save(@Valid @ModelAttribute SpeciesRequest request,
                       BindingResult result,
                       RedirectAttributes redirect) {
        if (result.hasErrors()) {
            redirect.addFlashAttribute("error", result.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/admin/species";
        }
        try {
            boolean isNew = request.getId() == null;
            speciesService.save(request);
            redirect.addFlashAttribute("success", isNew ? "เพิ่มพันธุ์ไม้แล้ว" : "แก้ไขพันธุ์ไม้แล้ว");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/species";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam Long id, RedirectAttributes redirect) {
        try {
            speciesService.delete(id);
            redirect.addFlashAttribute("success", "ลบพันธุ์ไม้แล้ว");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/species";
    }
}