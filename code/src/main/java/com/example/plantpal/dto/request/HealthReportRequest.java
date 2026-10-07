package com.example.plantpal.dto.request;

import com.example.plantpal.domain.enums.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

// ข้อมูลที่รับมาจากฟอร์มแจ้งปัญหาสุขภาพต้นไม้ (ชื่อ field ต้องตรงกับ name="" ใน report-form.html)
@Getter
@Setter
public class HealthReportRequest {

    @NotNull(message = "กรุณาเลือกต้นไม้")
    private Long plantId;

    @NotBlank(message = "กรุณากรอกหัวข้อ")
    @Size(max = 150, message = "หัวข้อยาวได้ไม่เกิน 150 ตัวอักษร")
    private String title;

    private String description;

    @NotNull(message = "กรุณาเลือกความรุนแรง")
    private Severity severity;

    // ไฟล์รูปที่ผู้ใช้อัปโหลด (ไม่บังคับ) Service จะส่งขึ้น Cloudinary แล้วเก็บแค่ URL ลง image_url
    private MultipartFile image;
}
