package com.example.plantpal.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.plantpal.exception.InvalidRequestException;
import com.example.plantpal.service.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CloudinaryImageStorageService implements ImageStorageService {

    // รับเฉพาะรูปพวกนี้ (ไม่รับ SVG เพราะฝังสคริปต์ได้) ใช้ชุดเดียวกับ LocalAvatarStorageService ของพรีม
    private static final Set<String> ALLOWED = Set.of("image/png", "image/jpeg", "image/gif", "image/webp");

    private final Cloudinary cloudinary;

    @Override
    public String upload(MultipartFile file, String folder) {
        // ข้อมูลที่ผู้ใช้ส่งมาผิด = InvalidRequestException (GlobalExceptionHandler แปลงเป็น 400)
        if (file == null || file.isEmpty()) {
            throw new InvalidRequestException("ไม่พบไฟล์รูป");
        }
        if (!ALLOWED.contains(file.getContentType())) {
            throw new InvalidRequestException("รองรับเฉพาะรูป PNG, JPG, GIF หรือ WEBP");
        }
        // ปัญหาฝั่งระบบ (ยังไม่ตั้ง key / Cloudinary ล่ม) = IllegalStateException (500) ไม่ใช่ความผิดของผู้ใช้
        if (cloudinary.config.cloudName == null) {
            throw new IllegalStateException("ระบบยังไม่ได้ตั้งค่าที่เก็บรูป (CLOUDINARY_URL) ลองส่งโดยไม่แนบรูปก่อน");
        }

        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "plantpal/" + folder,
                    "resource_type", "image"));
            // secure_url = ลิงก์ https ของรูปที่อัปโหลดแล้ว
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new IllegalStateException("อัปโหลดรูปไม่สำเร็จ ลองใหม่อีกครั้ง", e);
        }
    }
}
