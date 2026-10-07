package com.example.plantpal.service;

import com.example.plantpal.dto.request.ChangePasswordRequest;
import com.example.plantpal.dto.request.ProfileUpdateRequest;
import com.example.plantpal.dto.response.NavUser;
import com.example.plantpal.dto.response.ProfileView;
import org.springframework.web.multipart.MultipartFile;

// โปรไฟล์ของผู้ใช้ที่ login อยู่เท่านั้น (ไม่รับ id จากหน้าเว็บ กันแก้ของคนอื่น)
public interface ProfileService {

    ProfileView getMyProfile();

    void updateMyProfile(ProfileUpdateRequest request, MultipartFile avatar);

    void changeMyPassword(ChangePasswordRequest request);

    NavUser getNavUser();
}
