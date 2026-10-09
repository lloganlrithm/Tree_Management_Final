package com.example.plantpal.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

// ข้อมูลที่รับมาจากฟอร์มเพิ่ม/แก้ไขต้นไม้ (ชื่อ field ต้องตรงกับ name="" ในฟอร์ม)
@Getter
@Setter
public class PlantRequest {

    // null = เพิ่มใหม่, มีค่า = แก้ไขต้นเดิม
    private Long id;

    @NotNull(message = "กรุณาเลือกพันธุ์ไม้")
    private Long speciesId;

    @Size(max = 100, message = "ชื่อเล่นยาวได้ไม่เกิน 100 ตัวอักษร")
    private String nickname;

    @PastOrPresent(message = "วันที่ปลูกต้องไม่เป็นวันในอนาคต")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate plantedDate;
}