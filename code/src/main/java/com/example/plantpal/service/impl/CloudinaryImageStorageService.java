package com.example.plantpal.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.plantpal.service.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryImageStorageService implements ImageStorageService {

    private final Cloudinary cloudinary;

    @Override
    public String upload(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("ไม่พบไฟล์รูป");
        }
        // เช็คจากชนิดไฟล์ที่เบราว์เซอร์ส่งมา รับเฉพาะรูป (jpg, png, webp, ...)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("อัปโหลดได้เฉพาะไฟล์รูปภาพ");
        }
        // ยังไม่ได้ตั้ง CLOUDINARY_URL = ไม่รู้ว่าจะส่งไปบัญชีไหน
        if (cloudinary.config.cloudName == null) {
            throw new IllegalArgumentException("ระบบยังไม่ได้ตั้งค่าที่เก็บรูป (CLOUDINARY_URL) ลองส่งรายงานโดยไม่แนบรูปก่อน");
        }

        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "plantpal/" + folder,
                    "resource_type", "image"));
            // secure_url = ลิงก์ https ของรูปที่อัปโหลดแล้ว
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new IllegalArgumentException("อัปโหลดรูปไม่สำเร็จ ลองใหม่อีกครั้ง");
        }
    }
}
