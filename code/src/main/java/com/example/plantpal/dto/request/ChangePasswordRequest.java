package com.example.plantpal.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// ฟอร์ม "เปลี่ยนรหัสผ่าน" ในหน้าโปรไฟล์ (ตรวจใน ProfileService)
@Getter @Setter
@NoArgsConstructor
public class ChangePasswordRequest {
    private String currentPassword;
    private String newPassword;
    private String confirmPassword;
}
