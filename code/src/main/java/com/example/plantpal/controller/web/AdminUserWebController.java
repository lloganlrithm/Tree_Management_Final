package com.example.plantpal.controller.web;

import com.example.plantpal.domain.enums.Role;
import com.example.plantpal.exception.InvalidRequestException;
import com.example.plantpal.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserWebController {

    private final AdminUserService adminUserService;

    // E5 + U4: รายชื่อผู้ใช้ + ค้นหา (?q=) + แบ่งหน้า (?page=1,2,...)
    @GetMapping
    public String list(@RequestParam(defaultValue = "") String q,
                       @RequestParam(defaultValue = "1") int page,
                       Model model) {
        model.addAttribute("users", adminUserService.search(q, page - 1));
        model.addAttribute("q", q);
        model.addAttribute("lastCommand", adminUserService.lastCommandDescription());
        return "admin/users";
    }

    @PostMapping("/{id}/role")
    public String changeRole(@PathVariable Long id, @RequestParam Role role,
                             @RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "1") int page,
                             RedirectAttributes redirect) {
        return run(() -> adminUserService.changeRole(id, role), q, page, redirect);
    }

    // checkbox ที่ไม่ติ๊กจะไม่ส่งค่ามา เลยตั้ง default เป็น false (= ระงับ)
    @PostMapping("/{id}/active")
    public String setActive(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean active,
                            @RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "1") int page,
                            RedirectAttributes redirect) {
        return run(() -> adminUserService.setActive(id, active), q, page, redirect);
    }

    @PostMapping("/undo")
    public String undo(@RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "1") int page,
                       RedirectAttributes redirect) {
        String undone = adminUserService.undoLast();
        if (undone != null) {
            redirect.addFlashAttribute("success", "ย้อนกลับแล้ว: " + undone);
        }
        return backToList(q, page, redirect);
    }

    private String run(Runnable action, String q, int page, RedirectAttributes redirect) {
        try {
            action.run();
        } catch (InvalidRequestException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return backToList(q, page, redirect);
    }

    // กลับไปหน้าเดิม คำค้นและเลขหน้าเดิม
    private String backToList(String q, int page, RedirectAttributes redirect) {
        redirect.addAttribute("q", q);
        redirect.addAttribute("page", page);
        return "redirect:/admin/users";
    }
}
