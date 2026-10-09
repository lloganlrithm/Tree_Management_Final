package com.example.plantpal.controller.web;

import com.example.plantpal.domain.entity.Plant;
import com.example.plantpal.domain.entity.User;
import com.example.plantpal.domain.enums.HealthStatus;
import com.example.plantpal.dto.request.PlantRequest;
import com.example.plantpal.plant.state.InvalidHealthTransitionException;
import com.example.plantpal.plant.state.PlantHealthStates;
import com.example.plantpal.service.CareService;
import com.example.plantpal.service.CurrentUserService;
import com.example.plantpal.service.HealthReportService;
import com.example.plantpal.service.PlantService;
import com.example.plantpal.service.SpeciesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/plants")
@RequiredArgsConstructor
public class PlantController {

    private final PlantService plantService;
    private final SpeciesService speciesService;
    private final CurrentUserService currentUserService;
    private final HealthReportService healthReportService;
    private final CareService careService;

    // หน้ารายการต้นไม้ของฉัน (+ ค้นหาด้วย ?keyword=...)
    @GetMapping
    public String list(@RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("plants", plantService.findMyPlants(currentEmail(), keyword));
        model.addAttribute("speciesList", speciesService.findAll());   // ใช้ใน dropdown ของฟอร์มเพิ่มต้นไม้
        model.addAttribute("keyword", keyword);                        // ให้ช่องค้นหาจำคำที่พิมพ์ไว้
        return "plants/list";
    }

    // หน้ารายละเอียดต้นไม้ 1 ต้น
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        try {
            Plant plant = plantService.findMyPlant(id, currentEmail());
            model.addAttribute("plant", plant);
            model.addAttribute("speciesList", speciesService.findAll());   // ใช้ใน dropdown ของฟอร์มแก้ไข
            model.addAttribute("nextStatuses", PlantHealthStates.nextOf(plant.getHealthStatus()));   // ปุ่มเปลี่ยนสถานะ
            model.addAttribute("canUndo", plantService.canUndo(id));   // มีการแก้ไขให้ย้อนไหม (Memento)
            // รายงานสุขภาพของต้นนี้ ใช้ใน fragments/reports :: plantReports (ของเปรม)
            model.addAttribute("plantReports", healthReportService.findByPlant(id, currentEmail()));
            // ตารางดูแล + ประวัติการดูแลของต้นนี้ ใช้ใน fragments/care-detail :: plantCare (ของเปียโน)
            User me = currentUserService.getCurrentUser();
            model.addAttribute("plantSchedules", careService.findPlantSchedules(id, me));
            model.addAttribute("plantCareLogs", careService.findPlantHistory(id, me));
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
                       RedirectAttributes redirect) {
        // แก้ไขเสร็จกลับไปหน้า detail, เพิ่มใหม่กลับไปหน้า list
        String back = (request.getId() == null) ? "redirect:/plants" : "redirect:/plants/" + request.getId();

        if (result.hasErrors()) {
            redirect.addFlashAttribute("error", result.getAllErrors().get(0).getDefaultMessage());
            return back;
        }
        try {
            if (request.getId() == null) {
                plantService.create(request, currentEmail());
                redirect.addFlashAttribute("success", "เพิ่มต้นไม้แล้ว");
            } else {
                plantService.update(request, currentEmail());
                redirect.addFlashAttribute("success", "แก้ไขข้อมูลต้นไม้แล้ว");
            }
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return back;
    }

    // เปลี่ยนสถานะสุขภาพ: object สถานะปัจจุบันตัดสินว่าเปลี่ยนได้ไหม (State pattern)
    @PostMapping("/{id}/health")
    public String changeHealth(@PathVariable Long id,
                               @RequestParam HealthStatus status,
                               RedirectAttributes redirect) {
        try {
            plantService.changeMyPlantHealth(id, currentEmail(), status);
            redirect.addFlashAttribute("success", "เปลี่ยนสถานะสุขภาพแล้ว");
        } catch (IllegalArgumentException | InvalidHealthTransitionException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/plants/" + id;
    }

    // ย้อนการแก้ไขล่าสุด: เอาค่าเดิมจาก snapshot กลับมา (Memento pattern)
    @PostMapping("/{id}/undo")
    public String undo(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            plantService.undoLastEdit(id, currentEmail());
            redirect.addFlashAttribute("success", "ย้อนการแก้ไขล่าสุดแล้ว");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/plants/" + id;
    }

    @PostMapping("/delete")
    public String delete(@RequestParam Long id, RedirectAttributes redirect) {
        try {
            plantService.delete(id, currentEmail());
            redirect.addFlashAttribute("success", "ลบต้นไม้แล้ว");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/plants";
    }

    // อีเมลของคนที่ login อยู่ ถามผ่าน CurrentUserService ของพรีม (ตามกติกาทีม)
    private String currentEmail() {
        return currentUserService.getCurrentUser().getEmail();
    }
}