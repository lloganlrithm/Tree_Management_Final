package com.example.plantpal.service.impl;

import com.cloudinary.Cloudinary;
import com.example.plantpal.exception.InvalidRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

// ทดสอบการตรวจไฟล์ก่อนอัปโหลด (ไม่ส่งไป Cloudinary จริง: ใช้ Cloudinary ที่ไม่มี key)
class CloudinaryImageStorageServiceTest {

    private final CloudinaryImageStorageService service =
            new CloudinaryImageStorageService(new Cloudinary(Map.of()));

    @Test
    void emptyFileIsRejected() {
        MockMultipartFile empty = new MockMultipartFile("image", "leaf.png", "image/png", new byte[0]);

        assertThatThrownBy(() -> service.upload(empty, "reports"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("ไม่พบไฟล์รูป");
    }

    @Test
    void svgIsRejectedBecauseItCanContainScript() {
        MockMultipartFile svg = new MockMultipartFile("image", "x.svg", "image/svg+xml", "<svg/>".getBytes());

        assertThatThrownBy(() -> service.upload(svg, "reports"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("รองรับเฉพาะรูป PNG, JPG, GIF หรือ WEBP");
    }

    @Test
    void validImageWithoutCloudinaryKeyIsServerError() {
        // ไฟล์ถูกต้อง แต่ระบบยังไม่ได้ตั้ง CLOUDINARY_URL = ปัญหาฝั่งระบบ ไม่ใช่ความผิดผู้ใช้
        MockMultipartFile png = new MockMultipartFile("image", "leaf.png", "image/png", new byte[] {1, 2, 3});

        assertThatThrownBy(() -> service.upload(png, "reports"))
                .isInstanceOf(IllegalStateException.class);
    }
}
