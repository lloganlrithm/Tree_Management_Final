package com.example.plantpal.config;

import com.cloudinary.Cloudinary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

// สร้างตัวเชื่อม Cloudinary ไว้ให้ Service อื่น inject ไปใช้
@Slf4j
@Configuration
public class CloudinaryConfig {

    // ยังไม่ตั้ง / ตั้ง CLOUDINARY_URL ผิด ก็เปิดแอปได้ปกติ แค่จะอัปโหลดรูปไม่ได้ (เพื่อนที่ยังไม่มี key ไม่ติด)
    @Bean
    public Cloudinary cloudinary(@Value("${cloudinary.url:}") String cloudinaryUrl) {
        if (!cloudinaryUrl.isBlank()) {
            try {
                return new Cloudinary(cloudinaryUrl);
            } catch (IllegalArgumentException e) {
                log.warn("CLOUDINARY_URL ไม่ถูกต้อง (ต้องเป็น cloudinary://<api_key>:<api_secret>@<cloud_name>) ปิดการอัปโหลดรูปไว้ก่อน");
            }
        }
        // ใช้ Map ว่าง ไม่ใช้ new Cloudinary() เพราะตัวนั้นจะไปอ่าน CLOUDINARY_URL ที่ผิดซ้ำแล้วพังอีก
        return new Cloudinary(Map.of());
    }
}
