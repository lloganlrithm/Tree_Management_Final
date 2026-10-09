package com.example.plantpal.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

// รูปแบบ error มาตรฐานของ REST API ทุกตัว (/api/**)
// fieldErrors: ช่องไหนผิดเพราะอะไร (มีค่าเฉพาะตอน @Valid ไม่ผ่าน นอกนั้นเป็น {} )
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors) {

    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, path, Map.of());
    }
}
