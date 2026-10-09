package com.example.plantpal.validation;

import com.example.plantpal.dto.request.RegisterRequest;
import com.example.plantpal.exception.RegistrationException;

public class PasswordMatchValidator extends RegisterValidator {

    @Override
    protected void check(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RegistrationException("รหัสผ่านไม่ตรงกัน");
        }
    }
}
