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
            var user = userRepository.findByEmail(auth.getName());
            if (user.isPresent()) {
                return user.get();
            }
        }
        // TODO(U2): fallback ชั่วคราว ระหว่างที่ยังไม่มี login จริง ให้ใช้ user คนแรกในตาราง
        //           ต้องลบออกตอนทำ SecurityConfig แล้วโยน exception แทน
        return userRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("ยังไม่มีผู้ใช้ในตาราง users"));
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