package com.example.plantpal.validation;

import com.example.plantpal.dto.request.RegisterRequest;

// Chain of Responsibility: แต่ละตัวตรวจ 1 เรื่อง ผ่านแล้วส่งต่อให้ตัวถัดไป
// ไม่ผ่าน -> โยน RegistrationException แล้ว chain หยุดทันที
public abstract class RegisterValidator {

    private RegisterValidator next;

    // ต่อ chain: a.linkWith(b).linkWith(c) แล้วคืนตัวที่ต่อ เพื่อเขียนต่อกันได้
    public RegisterValidator linkWith(RegisterValidator next) {
        this.next = next;
        return next;
    }

    public void validate(RegisterRequest request) {
        check(request);
        if (next != null) {
            next.validate(request);
        }
    }

    protected abstract void check(RegisterRequest request);
}
