package com.example.plantpal.validation;

import com.example.plantpal.dto.request.RegisterRequest;
import com.example.plantpal.exception.RegistrationException;

public class PasswordLengthValidator extends RegisterValidator {

    private final int minLength;

    public PasswordLengthValidator(int minLength) {
        this.minLength = minLength;
    }

    @Override
    protected void check(RegisterRequest request) {
        String password = request.getPassword();
        if (password == null || password.length() < minLength) {
            throw new RegistrationException("รหัสผ่านต้องมีอย่างน้อย " + minLength + " ตัวอักษร");
        }
    }
}
