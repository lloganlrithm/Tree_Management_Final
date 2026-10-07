package com.example.plantpal.exception;

// หาข้อมูลไม่เจอ (GlobalExceptionHandler แปลงเป็น 404) ข้อความใช้แสดงผู้ใช้ได้เลย
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
