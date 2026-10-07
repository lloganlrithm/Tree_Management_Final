package com.example.plantpal.exception;

// ข้อมูลที่ส่งมาทำรายการไม่ได้ตามกฎของระบบ ข้อความใช้แสดงในหน้าเว็บได้เลย
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
