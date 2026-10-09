package com.example.plantpal.service;

import com.example.plantpal.domain.enums.Role;
import com.example.plantpal.dto.response.AdminUserRow;
import org.springframework.data.domain.Page;

public interface AdminUserService {

    // page เริ่มที่ 0
    Page<AdminUserRow> search(String keyword, int page);

    void changeRole(Long userId, Role role);

    void setActive(Long userId, boolean active);

    // คืนข้อความของคำสั่งที่ถูกย้อน หรือ null ถ้าไม่มี
    String undoLast();

    // ข้อความของคำสั่งล่าสุดที่ย้อนได้ (ไว้แสดงในแถบย้อนกลับ)
    String lastCommandDescription();
}
