package com.example.plantpal.service.impl;

import com.example.plantpal.service.AvatarStorageService;
import com.example.plantpal.service.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

// เก็บรูปโปรไฟล์ที่ Cloudinary แทนโฟลเดอร์ในเครื่อง (deploy บน Railway/Render แล้วรูปไม่หายตอน restart)
// Open/Closed: เพิ่ม class ใหม่ที่ implements AvatarStorageService โดยไม่แก้ LocalAvatarStorageService และ ProfileService ของพรีม
// @Primary: มีสองตัวให้ Spring เลือกตัวนี้ / @ConditionalOnExpression: สร้างเฉพาะตอนตั้ง CLOUDINARY_URL แล้ว
// ถ้ายังไม่ตั้ง (เช่น เครื่องเพื่อนที่ไม่มี key) จะใช้ LocalAvatarStorageService เหมือนเดิม
@Service
@Primary
@ConditionalOnExpression("!'${cloudinary.url:}'.isBlank()")
@RequiredArgsConstructor
public class CloudinaryAvatarStorageService implements AvatarStorageService {

    private final ImageStorageService imageStorageService;

    @Override
    public String store(MultipartFile file) {
        return imageStorageService.upload(file, "avatars");
    }
}
