package com.example.plantpal.validation;

import com.example.plantpal.dto.request.RegisterRequest;
import com.example.plantpal.exception.RegistrationException;

import java.util.regex.Pattern;

public class EmailFormatValidator extends RegisterValidator {

    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    @Override
    protected void check(RegisterRequest request) {
        String email = request.getEmail();
        if (email == null || !EMAIL.matcher(email).matches() || email.length() > 255) {
            throw new RegistrationException("รูปแบบอีเมลไม่ถูกต้อง");
        }
    }
}
