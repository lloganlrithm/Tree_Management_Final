package com.example.plantpal.service.impl;

import com.example.plantpal.exception.InvalidRequestException;
import com.example.plantpal.service.AvatarStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

// เก็บรูปโปรไฟล์ไว้ในโฟลเดอร์บนเครื่อง (app.upload-dir/avatars) แล้วเปิดให้ดูผ่าน /uploads/** (ดู WebConfig)
@Service
public class LocalAvatarStorageService implements AvatarStorageService {

    // รับเฉพาะรูปพวกนี้ (ไม่รับ SVG เพราะฝังสคริปต์ได้)
    private static final Map<String, String> ALLOWED = Map.of(
            "image/png", ".png",
            "image/jpeg", ".jpg",
            "image/gif", ".gif",
            "image/webp", ".webp");

    private final Path avatarDir;

    public LocalAvatarStorageService(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.avatarDir = Path.of(uploadDir, "avatars").toAbsolutePath().normalize();
    }

    @Override
    public String store(MultipartFile file) {
        String type = file.getContentType();
        String ext = (type == null) ? null : ALLOWED.get(type);
        if (ext == null) {
            throw new InvalidRequestException("รองรับเฉพาะรูป PNG, JPG, GIF หรือ WEBP");
        }
        try {
            Files.createDirectories(avatarDir);
            String name = UUID.randomUUID() + ext;   // ตั้งชื่อใหม่ กันชื่อซ้ำและชื่อไฟล์แปลก ๆ
            file.transferTo(avatarDir.resolve(name));
            return "/uploads/avatars/" + name;
        } catch (IOException e) {
            throw new IllegalStateException("บันทึกรูปไม่สำเร็จ", e);
        }
    }
}
