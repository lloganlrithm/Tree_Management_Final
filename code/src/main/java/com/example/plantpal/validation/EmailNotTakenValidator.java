package com.example.plantpal.validation;

import com.example.plantpal.dto.request.RegisterRequest;
import com.example.plantpal.exception.RegistrationException;
import com.example.plantpal.repository.UserRepository;

public class EmailNotTakenValidator extends RegisterValidator {

    private final UserRepository userRepository;

    public EmailNotTakenValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void check(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RegistrationException("อีเมลนี้ถูกใช้สมัครแล้ว");
        }
    }
}
