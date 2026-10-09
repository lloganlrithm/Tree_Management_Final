package com.example.plantpal.controller.web;

import com.example.plantpal.dto.response.NavUser;
import com.example.plantpal.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;


@ControllerAdvice(basePackages = "com.example.plantpal.controller.web")
@RequiredArgsConstructor
public class GlobalWebModelAdvice {

    private final ProfileService profileService;

    @ModelAttribute("isAdmin")
    public boolean isAdmin(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities().stream()
                        .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    // ชื่อและรูปมุมขวาบน (หน้า login/register ยังไม่ login เลยเป็น null)
    @ModelAttribute("navUser")
    public NavUser navUser(Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return profileService.getNavUser();
    }
}
