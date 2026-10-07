package com.example.plantpal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// ข้อมูลจากฟอร์มสมัครสมาชิก (auth/register.html)
// ชื่อ-นามสกุลตรวจด้วย @Valid ส่วนอีเมล/รหัสผ่านตรวจด้วย chain ใน validation/
@Getter @Setter
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "กรุณากรอกชื่อ")
    @Size(max = 100, message = "ชื่อยาวได้ไม่เกิน 100 ตัวอักษร")
    private String firstName;

    @NotBlank(message = "กรุณากรอกนามสกุล")
    @Size(max = 100, message = "นามสกุลยาวได้ไม่เกิน 100 ตัวอักษร")
    private String lastName;

    private String email;
    private String password;
    private String confirmPassword;
}
