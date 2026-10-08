package com.example.plantpal.service;

import com.example.plantpal.domain.entity.User;
import com.example.plantpal.dto.request.RegisterRequest;

import java.util.List;

public interface UserService {

    // สมัครสมาชิก: role = USER เสมอ, บันทึกทั้ง users และ user_profiles
    User register(RegisterRequest request);

    // id ของ admin ที่ยังเปิดใช้งานอยู่ทั้งหมด (ไม่รวมบัญชีที่ถูกระงับ)
    List<Long> findAdminIds();
}
