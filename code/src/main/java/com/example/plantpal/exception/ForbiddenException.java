package com.example.plantpal.exception;

// login แล้วแต่ไม่มีสิทธิ์ทำสิ่งนี้ เช่น ลบรายงานของคนอื่น (GlobalExceptionHandler แปลงเป็น 403)
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
