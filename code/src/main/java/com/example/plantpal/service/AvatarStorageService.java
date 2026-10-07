package com.example.plantpal.service;

import org.springframework.web.multipart.MultipartFile;

public interface AvatarStorageService {

    // เก็บรูปแล้วคืน URL สำหรับใส่ใน user_profiles.avatar_url
    String store(MultipartFile file);
}
