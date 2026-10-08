package com.example.plantpal.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI plantPalOpenApi() {
        return new OpenAPI().info(new Info()
                .title("PlantPal REST API")
                .version("v1")
                .description("""
                        REST API ของระบบดูแลต้นไม้ PlantPal (CP353002)
                        ต้อง login ที่ /login ก่อน แล้วค่อยกด Try it out (ใช้ session เดียวกับหน้าเว็บ)
                        error ทุกตัวตอบเป็น ErrorResponse: timestamp, status, error, message, path, fieldErrors"""));
    }
}
