package com.example.plantpal.service;

import com.example.plantpal.domain.entity.User;
import com.example.plantpal.dto.request.RegisterRequest;

public interface UserService {

    // สมัครสมาชิก: role = USER เสมอ, บันทึกทั้ง users และ user_profiles
    User register(RegisterRequest request);
}
