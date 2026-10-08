package com.example.plantpal.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

// เปิดใช้ @Scheduled ให้ job รายวัน (package job/) รันเองตามเวลา
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
