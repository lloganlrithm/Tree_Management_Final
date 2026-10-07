package com.example.plantpal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// ฟอร์ม "ข้อมูลส่วนตัว" ในหน้าโปรไฟล์ (รูปส่งแยกเป็น MultipartFile)
@Getter @Setter
@NoArgsConstructor
public class ProfileUpdateRequest {

    @NotBlank(message = "กรุณากรอกชื่อ")
    @Size(max = 100, message = "ชื่อยาวได้ไม่เกิน 100 ตัวอักษร")
    private String firstName;

    @NotBlank(message = "กรุณากรอกนามสกุล")
    @Size(max = 100, message = "นามสกุลยาวได้ไม่เกิน 100 ตัวอักษร")
    private String lastName;

    // ว่างได้ ถ้ากรอกต้องเป็นตัวเลข ขีด หรือ + เท่านั้น
    @Pattern(regexp = "^[0-9+\\- ]{0,20}$", message = "เบอร์โทรศัพท์ไม่ถูกต้อง")
    private String phoneNumber;
}
