package com.example.plantpal.service.impl;

import com.example.plantpal.domain.entity.User;
import com.example.plantpal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Spring Security เรียกตอน login: หา user จากอีเมลในตาราง users
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new UsernameNotFoundException("ไม่พบผู้ใช้: " + email));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())          // BCrypt hash
                .roles(user.getRole().name())          // USER -> ROLE_USER, ADMIN -> ROLE_ADMIN
                .disabled(!user.getIsActive())         // is_active = false -> login ไม่ได้
                .build();
    }
}
