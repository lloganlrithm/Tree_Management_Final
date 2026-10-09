package com.example.plantpal.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

// ผู้ใช้กด "ยังไม่ดีขึ้น" หลังทำตามคำแนะนำ: บอกอาการตอนนี้ + แนบรูปใหม่ได้ (ไม่บังคับทั้งคู่)
@Getter
@Setter
public class ReportFollowUpRequest {

    @Size(max = 2000, message = "อาการยาวได้ไม่เกิน 2000 ตัวอักษร")
    private String message;

    // ส่งไฟล์ผ่าน JSON ของ REST API ไม่ได้ ให้ข้าม field นี้ (ฟอร์มหน้าเว็บยังแนบรูปได้)
    @JsonIgnore
    private MultipartFile image;
}
