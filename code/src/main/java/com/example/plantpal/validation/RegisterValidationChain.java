package com.example.plantpal.validation;

import com.example.plantpal.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// ประกอบ chain ตามลำดับ: อีเมลถูกรูปแบบ -> อีเมลไม่ซ้ำ -> รหัสยาวพอ -> รหัสตรงกัน
// จะเพิ่ม/สลับลำดับการตรวจ แก้ที่นี่ที่เดียว ไม่ต้องแตะ UserService
@Configuration
public class RegisterValidationChain {

    @Bean
    public RegisterValidator registerValidator(UserRepository userRepository) {
        RegisterValidator first = new EmailFormatValidator();
        first.linkWith(new EmailNotTakenValidator(userRepository))
             .linkWith(new PasswordLengthValidator(8))
             .linkWith(new PasswordMatchValidator());
        return first;
    }
}
