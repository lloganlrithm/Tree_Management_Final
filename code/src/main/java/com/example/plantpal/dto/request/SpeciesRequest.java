package com.example.plantpal.dto.request;

import com.example.plantpal.domain.enums.SunlightRequirement;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

// ข้อมูลที่รับมาจากฟอร์มเพิ่ม/แก้ไขพันธุ์ไม้ (ชื่อ field ต้องตรงกับ name="" ใน species.html)
@Getter
@Setter
public class SpeciesRequest {

    // null = เพิ่มใหม่, มีค่า = แก้ไขตัวเดิม
    private Long id;

    @NotBlank(message = "กรุณากรอกชื่อพันธุ์ไม้")
    @Size(max = 100, message = "ชื่อพันธุ์ไม้ยาวได้ไม่เกิน 100 ตัวอักษร")
    private String name;

    @NotNull(message = "กรุณากรอกรอบรดน้ำ")
    @Min(value = 1, message = "รอบรดน้ำต้องอย่างน้อย 1 วัน")
    @Max(value = 365, message = "รอบรดน้ำต้องไม่เกิน 365 วัน")
    private Integer waterIntervalDays;

    // เว้นว่างได้ (NULL = ใช้ค่าตั้งต้นใน Strategy)
    @Min(value = 1, message = "รอบใส่ปุ๋ยต้องอย่างน้อย 1 วัน")
    private Integer fertilizeIntervalDays;

    @Min(value = 1, message = "รอบเปลี่ยนกระถางต้องอย่างน้อย 1 วัน")
    private Integer repotIntervalDays;

    private SunlightRequirement sunlightRequirement;

    private String description;
}