package com.example.plantpal.exception;

// ข้อมูลสมัครสมาชิกไม่ผ่านการตรวจ ข้อความใช้แสดงในหน้าเว็บได้เลย
public class RegistrationException extends RuntimeException {

    public RegistrationException(String message) {
        super(message);
    }
}
