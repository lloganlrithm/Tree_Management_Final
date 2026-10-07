package com.example.plantpal.event;

// ส่งออกไปหลังเพิ่มต้นไม้ใหม่ ใครสนใจ (เช่น care ของเปียโน) ก็ฟังได้ (Observer)
public record PlantCreatedEvent(Long plantId) {
}