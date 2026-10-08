package com.example.plantpal.service.impl;

import com.example.plantpal.domain.entity.User;
import com.example.plantpal.domain.entity.UserProfile;
import com.example.plantpal.domain.enums.Role;
import com.example.plantpal.dto.request.RegisterRequest;
import com.example.plantpal.repository.UserRepository;
import com.example.plantpal.service.UserService;
import com.example.plantpal.validation.RegisterValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegisterValidator registerValidator;   // หัวของ chain

    @Override
    public User register(RegisterRequest request) {
        // อีเมลเก็บเป็นตัวเล็กเสมอ กันสมัครซ้ำด้วย A@x.com กับ a@x.com
        if (request.getEmail() != null) {
            request.setEmail(request.getEmail().trim().toLowerCase());
        }
        registerValidator.validate(request);

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        UserProfile profile = UserProfile.builder()
                .user(user)
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .build();
        user.setProfile(profile);

        return userRepository.save(user);   // cascade บันทึก user_profiles ให้ด้วย
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> findAdminIds() {
        return userRepository.findActiveIdsByRole(Role.ADMIN);
    }
}
