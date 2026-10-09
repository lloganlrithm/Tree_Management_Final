package com.example.plantpal.service;

import org.springframework.web.multipart.MultipartFile;

// อัปโหลดรูปแล้วคืน URL ไปเก็บใน DB (เช่น health_reports.image_url, user_profiles.avatar_url)
// Service อื่นรู้จักแค่ interface นี้ ไม่รู้ว่าข้างหลังเป็น Cloudinary จะเปลี่ยนที่เก็บทีหลังก็ไม่ต้องแก้คนเรียก
public interface ImageStorageService {

    // folder = โฟลเดอร์ย่อยใน Cloudinary เช่น "reports", "avatars"
    String upload(MultipartFile file, String folder);
}
