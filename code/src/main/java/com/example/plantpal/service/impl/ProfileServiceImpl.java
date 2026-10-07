package com.example.plantpal.service.impl;

import com.example.plantpal.domain.entity.User;
import com.example.plantpal.domain.entity.UserProfile;
import com.example.plantpal.dto.request.ChangePasswordRequest;
import com.example.plantpal.dto.request.ProfileUpdateRequest;
import com.example.plantpal.dto.response.NavUser;
import com.example.plantpal.dto.response.ProfileView;
import com.example.plantpal.exception.InvalidRequestException;
import com.example.plantpal.repository.UserRepository;
import com.example.plantpal.service.AvatarStorageService;
import com.example.plantpal.service.CurrentUserService;
import com.example.plantpal.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfileServiceImpl implements ProfileService {

    private static final int MIN_PASSWORD_LENGTH = 8;

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AvatarStorageService avatarStorageService;

    @Override
    @Transactional(readOnly = true)
    public ProfileView getMyProfile() {
        User user = currentUserService.getCurrentUser();
        UserProfile p = user.getProfile();
        return new ProfileView(
                user.getEmail(),
                p == null ? null : p.getFirstName(),
                p == null ? null : p.getLastName(),
                p == null ? null : p.getPhoneNumber(),
                p == null ? null : p.getAvatarUrl(),
                user.getRole(),
                user.getCreatedAt(),
                userRepository.countPlantsByUserId(user.getId()),
                userRepository.countCareLogsByUserId(user.getId()));
    }

    @Override
    public void updateMyProfile(ProfileUpdateRequest request, MultipartFile avatar) {
        User user = currentUserService.getCurrentUser();
        UserProfile profile = user.getProfile();
        if (profile == null) {                       // บัญชีเก่าที่ยังไม่มีแถวใน user_profiles
            profile = UserProfile.builder().user(user).build();
            user.setProfile(profile);
        }
        profile.setFirstName(request.getFirstName().trim());
        profile.setLastName(request.getLastName().trim());
        profile.setPhoneNumber(StringUtils.hasText(request.getPhoneNumber())
                ? request.getPhoneNumber().trim() : null);

        if (avatar != null && !avatar.isEmpty()) {   // ไม่เลือกรูปใหม่ = ใช้รูปเดิม
            profile.setAvatarUrl(avatarStorageService.store(avatar));
        }
        userRepository.save(user);
    }

    @Override
    public void changeMyPassword(ChangePasswordRequest request) {
        User user = currentUserService.getCurrentUser();

        if (request.getCurrentPassword() == null
                || !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidRequestException("รหัสผ่านปัจจุบันไม่ถูกต้อง");
        }
        String newPassword = request.getNewPassword();
        if (newPassword == null || newPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new InvalidRequestException("รหัสผ่านใหม่ต้องมีอย่างน้อย " + MIN_PASSWORD_LENGTH + " ตัวอักษร");
        }
        if (!newPassword.equals(request.getConfirmPassword())) {
            throw new InvalidRequestException("รหัสผ่านใหม่ไม่ตรงกัน");
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new InvalidRequestException("รหัสผ่านใหม่ต้องไม่ซ้ำกับรหัสเดิม");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public NavUser getNavUser() {
        User user = currentUserService.getCurrentUser();
        UserProfile p = user.getProfile();
        String name = (p != null && StringUtils.hasText(p.getFirstName())) ? p.getFirstName() : user.getEmail();
        return new NavUser(name, p == null ? null : p.getAvatarUrl());
    }
}
