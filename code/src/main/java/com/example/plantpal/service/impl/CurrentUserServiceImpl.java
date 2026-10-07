package com.example.plantpal.service.impl;

import com.example.plantpal.domain.entity.User;
import com.example.plantpal.domain.enums.Role;
import com.example.plantpal.repository.UserRepository;
import com.example.plantpal.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CurrentUserServiceImpl implements CurrentUserService {

    private final UserRepository userRepository;

    @Override
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            // username ที่ login = อีเมลในตาราง users
            return userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new IllegalStateException("ไม่พบผู้ใช้ที่ login อยู่ในตาราง users"));
        }
        // SecurityConfig บังคับ login ทุกหน้าที่เรียก service อยู่แล้ว ถ้ามาถึงตรงนี้แปลว่าเรียกผิดที่
        throw new IllegalStateException("ยังไม่ได้ login");
    }

    @Override
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    @Override
    public boolean isAdmin() {
        return getCurrentUser().getRole() == Role.ADMIN;
    }
}