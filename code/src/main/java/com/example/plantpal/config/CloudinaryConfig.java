package com.example.plantpal.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// สร้างตัวเชื่อม Cloudinary ไว้ให้ Service อื่น inject ไปใช้
@Configuration
public class CloudinaryConfig {

    // ยังไม่ตั้ง CLOUDINARY_URL ก็เปิดแอปได้ปกติ แค่จะอัปโหลดรูปไม่ได้ (เพื่อนที่ยังไม่มี key ไม่ติด)
    @Bean
    public Cloudinary cloudinary(@Value("${cloudinary.url:}") String cloudinaryUrl) {
        return cloudinaryUrl.isBlank() ? new Cloudinary() : new Cloudinary(cloudinaryUrl);
    }
}
